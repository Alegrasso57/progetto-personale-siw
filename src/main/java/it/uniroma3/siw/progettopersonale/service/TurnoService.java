package it.uniroma3.siw.progettopersonale.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.RichiestaAdozione;
import it.uniroma3.siw.progettopersonale.model.Ruolo;
import it.uniroma3.siw.progettopersonale.model.Turno;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.repository.AnimaleRepository;
import it.uniroma3.siw.progettopersonale.repository.TurnoRepository;
import it.uniroma3.siw.progettopersonale.repository.UtenteRepository;

@Service
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final AnimaleRepository animaleRepository;
    private final UtenteRepository utenteRepository;

    public TurnoService(TurnoRepository turnoRepository, AnimaleRepository animaleRepository, UtenteRepository utenteRepository) {
        this.turnoRepository = turnoRepository;
        this.animaleRepository = animaleRepository;
        this.utenteRepository = utenteRepository;
    }

    @Transactional(readOnly = true)
    public List<Turno> findAll() {
        return turnoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Turno> findByData(LocalDate data) {
        return turnoRepository.findByDataOrderByOraInizioAsc(data);
    }

    @Transactional(readOnly = true)
    public List<Turno> findByAnimaleId(Long animaleId) {
        Animale animale = animaleRepository.findById(animaleId).orElse(null);
        if (animale == null) {
            throw new IllegalArgumentException("Animale non trovato");
        }
        return turnoRepository.findByAnimale(animale);
    }

    @Transactional(readOnly = true)
    public Turno findById(Long id) {
        return turnoRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Turno> findByVolontario(Utente volontario) {
        return turnoRepository.findByVolontario(volontario);
    }

    @Transactional(readOnly = true)
    public List<Turno> findByVolontarioAndData(Utente volontario, LocalDate data) {
        return turnoRepository.findByVolontarioAndData(volontario, data);
    }

    /** Turni dichiarati disponibili dai volontari (non ancora legati a un animale/richiesta),
     *  a partire da oggi, utilizzabili da un adottante per prenotare una visita. */
    @Transactional(readOnly = true)
    public List<Turno> findDisponibiliPerPrenotazione() {
        LocalDate oggi = LocalDate.now();
        return turnoRepository.findByAnimaleIsNullOrderByDataAscOraInizioAsc().stream()
                .filter(turno -> !turno.getData().isBefore(oggi))
                .collect(Collectors.toList());
    }

    /** Turni da mostrare nel form di modifica di una richiesta: i turni ancora liberi,
     *  piu' quelli gia' prenotati per questa stessa richiesta (che altrimenti non
     *  comparirebbero, avendo gia' un animale/richiesta associati), ordinati per data/ora. */
    @Transactional(readOnly = true)
    public List<Turno> findDisponibiliPerModifica(RichiestaAdozione richiesta) {
        List<Turno> turni = new ArrayList<>(turnoRepository.findByRichiestaAdozione(richiesta));
        turni.addAll(findDisponibiliPerPrenotazione());
        turni.sort(Comparator.comparing(Turno::getData).thenComparing(Turno::getOraInizio));
        return turni;
    }

    /** Il volontario dichiara una propria disponibilita' (nessun animale/richiesta associati). */
    @Transactional
    public Turno creaTurno(Long volontarioId, LocalDate data, LocalTime oraInizio, LocalTime oraFine, String note) {
        Utente volontario = utenteRepository.findById(volontarioId).orElse(null);
        if (volontario == null || volontario.getRuolo() != Ruolo.VOLONTARIO) {
            throw new IllegalArgumentException("Volontario non valido");
        }
        if (oraInizio == null || oraFine == null || !oraFine.isAfter(oraInizio)) {
            throw new IllegalArgumentException("L'ora di fine deve essere successiva all'ora di inizio");
        }
        boolean sovrapposto = turnoRepository.findByVolontarioAndData(volontario, data).stream()
                .anyMatch(t -> oraInizio.isBefore(t.getOraFine()) && t.getOraInizio().isBefore(oraFine));
        if (sovrapposto) {
            throw new IllegalArgumentException("Esiste gia' un turno che si sovrappone in questa fascia oraria");
        }
        Turno turno = new Turno();
        turno.setVolontario(volontario);
        turno.setData(data);
        turno.setOraInizio(oraInizio);
        turno.setOraFine(oraFine);
        turno.setNote(note);
        return turnoRepository.save(turno);
    }

    /** Il volontario modifica un proprio turno; volontarioId e' l'utente che sta effettuando
     *  l'operazione ed e' usato per verificarne la proprieta'. */
    @Transactional
    public Turno modificaTurno(Long id, Long volontarioId, LocalDate data, LocalTime oraInizio, LocalTime oraFine, String note) {
        Turno turno = turnoRepository.findById(id).orElse(null);
        if (turno == null) {
            throw new IllegalArgumentException("Turno non trovato");
        }
        if (turno.getVolontario() == null || !turno.getVolontario().getId().equals(volontarioId)) {
            throw new IllegalStateException("Non sei autorizzato a modificare questo turno");
        }
        if (oraInizio == null || oraFine == null || !oraFine.isAfter(oraInizio)) {
            throw new IllegalArgumentException("L'ora di fine deve essere successiva all'ora di inizio");
        }
        boolean sovrapposto = turnoRepository.findByVolontarioAndData(turno.getVolontario(), data).stream()
                .anyMatch(t -> !t.getId().equals(id) && oraInizio.isBefore(t.getOraFine()) && t.getOraInizio().isBefore(oraFine));
        if (sovrapposto) {
            throw new IllegalArgumentException("Esiste gia' un turno che si sovrappone in questa fascia oraria");
        }
        turno.setData(data);
        turno.setOraInizio(oraInizio);
        turno.setOraFine(oraFine);
        turno.setNote(note);
        return turnoRepository.save(turno);
    }

    /** Elimina un turno verificando che appartenga al volontario che ne fa richiesta. */
    @Transactional
    public void deleteById(Long id, Long volontarioId) {
        Turno turno = turnoRepository.findById(id).orElse(null);
        if (turno == null) {
            return;
        }
        if (turno.getVolontario() == null || !turno.getVolontario().getId().equals(volontarioId)) {
            throw new IllegalStateException("Non sei autorizzato a eliminare questo turno");
        }
        turnoRepository.deleteById(id);
    }
}
