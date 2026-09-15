package it.uniroma3.siw.progettopersonale.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.progettopersonale.exception.OperazioneNonConsentitaException;
import it.uniroma3.siw.progettopersonale.exception.TurnoNonTrovatoException;
import it.uniroma3.siw.progettopersonale.model.RichiestaAdozione;
import it.uniroma3.siw.progettopersonale.model.Turno;
import it.uniroma3.siw.progettopersonale.repository.TurnoRepository;

/**
 * Slot di disponibilita' per le visite. Li crea, modifica ed elimina solo l'ADMIN.
 *
 * Qui stanno le REGOLE DI BUSINESS sui turni:
 *   - l'ora di fine deve essere successiva a quella di inizio
 *   - due turni nella stessa data non possono sovrapporsi
 *   - un turno gia' prenotato da una richiesta non si puo' modificare ne' eliminare
 */
@Service
public class TurnoService {

    private static final Logger logger = LoggerFactory.getLogger(TurnoService.class);

    private final TurnoRepository turnoRepository;

    public TurnoService(TurnoRepository turnoRepository) {
        this.turnoRepository = turnoRepository;
    }

    @Transactional(readOnly = true)
    public Turno findById(Long id) {
        return turnoRepository.findById(id).orElseThrow(() -> new TurnoNonTrovatoException(id));
    }

    /** Tutti i turni inseriti. */
    @Transactional(readOnly = true)
    public List<Turno> findTutti() {
        List<Turno> risultato = new ArrayList<>();
        turnoRepository.findAll().forEach(risultato::add);
        return risultato;
    }

    /**
     * Slot ancora prenotabili: liberi e non gia' passati. Sono quelli che
     * l'utente vede quando invia o modifica una richiesta di adozione.
     */
    @Transactional(readOnly = true)
    public List<Turno> findPrenotabili() {
        LocalDate oggi = LocalDate.now();
        List<Turno> risultato = new ArrayList<>();
        for (Turno turno : turnoRepository.findByRichiestaAdozioneIsNull()) {
            if (!turno.getData().isBefore(oggi)) {
                risultato.add(turno);
            }
        }
        return risultato;
    }

    /**
     * Controlli comuni a creazione e modifica.
     *
     * SOVRAPPOSIZIONE: due fasce orarie dello stesso giorno si sovrappongono
     * quando ciascuna inizia prima che l'altra finisca, cioe' quando
     * (nuovoInizio < esistenteFine) AND (esistenteInizio < nuovoFine).
     * Se due turni si toccano soltanto (09:00-10:00 e 10:00-11:00) la
     * condizione e' falsa, quindi sono ammessi.
     *
     * idDaIgnorare serve in modifica: il turno che sto modificando non deve
     * essere confrontato con se stesso, altrimenti risulterebbe sempre
     * sovrapposto.
     */
    private void verificaFascia(LocalDate data, LocalTime oraInizio, LocalTime oraFine, Long idDaIgnorare) {

        if (!oraFine.isAfter(oraInizio)) {
            throw new OperazioneNonConsentitaException("L'ora di fine deve essere successiva all'ora di inizio.");
        }

        for (Turno esistente : turnoRepository.findByData(data)) {
            if (idDaIgnorare != null && idDaIgnorare.equals(esistente.getId())) {
                continue;
            }
            boolean sovrapposti = oraInizio.isBefore(esistente.getOraFine())
                    && esistente.getOraInizio().isBefore(oraFine);
            if (sovrapposti) {
                throw new OperazioneNonConsentitaException(
                        "Esiste già un turno che si sovrappone a questa fascia oraria.");
            }
        }
    }

    /** L'admin inserisce un nuovo slot di disponibilita'. */
    @Transactional
    public Turno creaTurno(LocalDate data, LocalTime oraInizio, LocalTime oraFine) {

        verificaFascia(data, oraInizio, oraFine, null);

        Turno turno = new Turno();
        turno.setData(data);
        turno.setOraInizio(oraInizio);
        turno.setOraFine(oraFine);

        turno = turnoRepository.save(turno);
        logger.info("Turno creato: data={}, {}-{}", data, oraInizio, oraFine);
        return turno;
    }

    /**
     * L'admin modifica uno slot esistente. Non si puo' modificare uno slot che
     * un utente ha gia' prenotato: cambiargli data o orario sotto il naso
     * falserebbe l'appuntamento gia' fissato.
     */
    @Transactional
    public Turno modificaTurno(Long id, LocalDate data, LocalTime oraInizio, LocalTime oraFine) {

        Turno turno = findById(id);
        if (turno.getRichiestaAdozione() != null) {
            throw new OperazioneNonConsentitaException(
                    "Questo turno è già stato prenotato da una richiesta di adozione e non può essere modificato.");
        }

        verificaFascia(data, oraInizio, oraFine, id);

        turno.setData(data);
        turno.setOraInizio(oraInizio);
        turno.setOraFine(oraFine);

        turno = turnoRepository.save(turno);
        logger.info("Turno modificato: id={}, data={}, {}-{}", id, data, oraInizio, oraFine);
        return turno;
    }

    /** L'admin elimina uno slot, ma solo se non e' gia' stato prenotato. */
    @Transactional
    public void eliminaTurno(Long id) {
        Turno turno = findById(id);
        if (turno.getRichiestaAdozione() != null) {
            throw new OperazioneNonConsentitaException(
                    "Questo turno è già stato prenotato da una richiesta di adozione e non può essere eliminato.");
        }
        turnoRepository.delete(turno);
        logger.info("Turno eliminato: id={}", id);
    }

    /** Prenota uno slot per una richiesta, verificando che sia ancora libero. */
    @Transactional
    public Turno prenota(Long turnoId, RichiestaAdozione richiesta) {
        Turno turno = findById(turnoId);
        if (turno.getRichiestaAdozione() != null) {
            throw new OperazioneNonConsentitaException(
                    "Lo slot scelto non è più disponibile: aggiorna la pagina e riprova.");
        }
        if (turno.getData().isBefore(LocalDate.now())) {
            throw new OperazioneNonConsentitaException("Lo slot scelto è ormai passato: aggiorna la pagina e riprova.");
        }
        turno.setRichiestaAdozione(richiesta);
        return turnoRepository.save(turno);
    }

    /** Libera lo slot prenotato da una richiesta (quando viene rifiutata, modificata o cancellata). */
    @Transactional
    public void liberaTurnoDi(RichiestaAdozione richiesta) {
        turnoRepository.findByRichiestaAdozione(richiesta).ifPresent(turno -> {
            turno.setRichiestaAdozione(null);
            turnoRepository.save(turno);
        });
    }
}
