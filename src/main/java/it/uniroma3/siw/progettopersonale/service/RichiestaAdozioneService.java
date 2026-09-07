package it.uniroma3.siw.progettopersonale.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.RichiestaAdozione;
import it.uniroma3.siw.progettopersonale.model.Ruolo;
import it.uniroma3.siw.progettopersonale.model.StatoAnimale;
import it.uniroma3.siw.progettopersonale.model.StatoRichiesta;
import it.uniroma3.siw.progettopersonale.model.Turno;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.repository.AnimaleRepository;
import it.uniroma3.siw.progettopersonale.repository.RichiestaAdozioneRepository;
import it.uniroma3.siw.progettopersonale.repository.TurnoRepository;
import it.uniroma3.siw.progettopersonale.repository.UtenteRepository;

@Service
public class RichiestaAdozioneService {

    private final RichiestaAdozioneRepository richiestaAdozioneRepository;
    private final AnimaleRepository animaleRepository;
    private final UtenteRepository utenteRepository;
    private final TurnoRepository turnoRepository;

    public RichiestaAdozioneService(RichiestaAdozioneRepository richiestaAdozioneRepository,
                                     AnimaleRepository animaleRepository,
                                     UtenteRepository utenteRepository,
                                     TurnoRepository turnoRepository) {
        this.richiestaAdozioneRepository = richiestaAdozioneRepository;
        this.animaleRepository = animaleRepository;
        this.utenteRepository = utenteRepository;
        this.turnoRepository = turnoRepository;
    }

    @Transactional(readOnly = true)
    public List<RichiestaAdozione> findAll() {
        return richiestaAdozioneRepository.findAll();
    }

    @Transactional(readOnly = true)
    public RichiestaAdozione findById(Long id) {
        return richiestaAdozioneRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<RichiestaAdozione> findByAnimaleId(Long animaleId) {
        Animale animale = animaleRepository.findById(animaleId).orElse(null);
        if (animale == null) {
            throw new IllegalArgumentException("Animale non trovato");
        }
        return richiestaAdozioneRepository.findByAnimale(animale);
    }

    @Transactional(readOnly = true)
    public List<RichiestaAdozione> findByAdottanteId(Long adottanteId) {
        Utente adottante = utenteRepository.findById(adottanteId).orElse(null);
        if (adottante == null) {
            throw new IllegalArgumentException("Utente non trovato");
        }
        return richiestaAdozioneRepository.findByAdottante(adottante);
    }

    @Transactional(readOnly = true)
    public List<RichiestaAdozione> findInAttesa() {
        return richiestaAdozioneRepository.findByStato(StatoRichiesta.IN_ATTESA);
    }

    /** Filtra le richieste in attesa per nome animale o adottante. */
    @Transactional(readOnly = true)
    public List<RichiestaAdozione> findInAttesaBySearch(String q) {
        if (q == null || q.isBlank()) {
            return findInAttesa();
        }
        String lower = q.trim().toLowerCase();
        return findInAttesa().stream()
            .filter(r -> r.getAnimale().getNome().toLowerCase().contains(lower)
                      || r.getAdottante().getNome().toLowerCase().contains(lower)
                      || r.getAdottante().getCognome().toLowerCase().contains(lower))
            .toList();
    }

    /** Filtra le richieste di un adottante per nome animale. */
    @Transactional(readOnly = true)
    public List<RichiestaAdozione> findByAdottanteIdAndSearch(Long adottanteId, String q) {
        List<RichiestaAdozione> tutte = findByAdottanteId(adottanteId);
        if (q == null || q.isBlank()) {
            return tutte;
        }
        String lower = q.trim().toLowerCase();
        return tutte.stream()
            .filter(r -> r.getAnimale().getNome().toLowerCase().contains(lower))
            .toList();
    }

    @Transactional
    public RichiestaAdozione creaRichiesta(Long animaleId, Long adottanteId, String motivazione,
                                            List<Long> turnoIdsSelezionati) {

        Animale animale = animaleRepository.findById(animaleId).orElse(null);
        if (animale == null) {
            throw new IllegalArgumentException("Animale non trovato");
        }
        if (animale.getStato() != StatoAnimale.DISPONIBILE) {
            throw new IllegalStateException("L'animale non è al momento disponibile per l'adozione");
        }

        Utente adottante = utenteRepository.findById(adottanteId).orElse(null);
        if (adottante == null) {
            throw new IllegalArgumentException("Utente non trovato");
        }
        if (adottante.getRuolo() != Ruolo.ADOTTANTE) {
            throw new IllegalStateException("Solo un adottante può inviare una richiesta di adozione");
        }

        boolean giaRichiesta = richiestaAdozioneRepository
                .findByAdottanteAndAnimaleAndStato(adottante, animale, StatoRichiesta.IN_ATTESA)
                .isPresent();
        if (giaRichiesta) {
            throw new IllegalStateException("Hai già una richiesta in attesa per questo animale");
        }

        if (turnoIdsSelezionati == null || turnoIdsSelezionati.isEmpty()) {
            throw new IllegalStateException("Devi selezionare necessariamente un turno per effettuare la visita");
        }

        // Ricontrolla al momento del salvataggio che i turni scelti siano ancora
        // liberi (non fidandosi di quanto visto dall'adottante quando ha caricato
        // la pagina: nel frattempo qualcun altro potrebbe averli prenotati) e che
        // non si sovrappongano a un turno di un altro volontario già prenotato per
        // lo stesso animale (altrimenti lo stesso animale finirebbe "impegnato" in
        // due visite diverse nello stesso momento).
        List<Turno> turniGiaPrenotatiPerAnimale = turnoRepository.findByAnimale(animale);
        List<Turno> turniDaPrenotare = new ArrayList<>();
        LocalDate oggi = LocalDate.now();
        for (Long turnoId : turnoIdsSelezionati) {
            Turno turno = turnoRepository.findById(turnoId).orElse(null);
            if (turno == null) {
                throw new IllegalArgumentException("Uno dei turni scelti non esiste più");
            }
            if (turno.getAnimale() != null || turno.getRichiestaAdozione() != null) {
                throw new IllegalStateException(
                        "Uno dei turni scelti non è più disponibile: aggiorna la pagina e riprova");
            }
            if (turno.getData().isBefore(oggi)) {
                throw new IllegalStateException("Uno dei turni scelti è ormai passato: aggiorna la pagina e riprova");
            }
            if (sovrappostoPerAnimale(turno, turniGiaPrenotatiPerAnimale)
                    || sovrappostoPerAnimale(turno, turniDaPrenotare)) {
                throw new IllegalStateException(
                        "Uno dei turni scelti si sovrappone a un'altra visita già prenotata per questo animale: aggiorna la pagina e riprova");
            }
            turniDaPrenotare.add(turno);
        }

        RichiestaAdozione richiesta = new RichiestaAdozione();
        richiesta.setAnimale(animale);
        richiesta.setAdottante(adottante);
        richiesta.setMotivazione(motivazione);
        richiesta.setDataRichiesta(LocalDate.now());
        richiesta.setStato(StatoRichiesta.IN_ATTESA);
        richiesta = richiestaAdozioneRepository.save(richiesta);

        for (Turno turno : turniDaPrenotare) {
            turno.setAnimale(animale);
            turno.setRichiestaAdozione(richiesta);
            turnoRepository.save(turno);
        }

        return richiesta;
    }

    /** Vero se il turno candidato si sovrappone (stessa data, fasce orarie che si
     *  intersecano) a uno dei turni già prenotati per lo stesso animale, tenuti da
     *  un volontario diverso: lo stesso animale non può risultare impegnato in due
     *  visite nello stesso momento. */
    private boolean sovrappostoPerAnimale(Turno candidato, List<Turno> turniEsistenti) {
        for (Turno esistente : turniEsistenti) {
            if (esistente.getId() != null && esistente.getId().equals(candidato.getId())) {
                continue;
            }
            boolean stessaData = esistente.getData().equals(candidato.getData());
            boolean orariSovrapposti = candidato.getOraInizio().isBefore(esistente.getOraFine())
                    && esistente.getOraInizio().isBefore(candidato.getOraFine());
            if (stessaData && orariSovrapposti) {
                return true;
            }
        }
        return false;
    }

    /**
     * Cambia i turni prenotati per una richiesta ancora IN_ATTESA, solo se appartiene
     * all'utente che la sta modificando. I turni non più selezionati tornano liberi,
     * quelli nuovi vengono prenotati dopo aver ripetuto gli stessi controlli di
     * creaRichiesta (liberi, non passati, non sovrapposti ad altre visite dello
     * stesso animale) — quelli già prenotati per questa stessa richiesta e
     * riselezionati restano invariati.
     */
    @Transactional
    public RichiestaAdozione modificaTurniPrenotati(Long richiestaId, String usernameRichiedente,
                                                     List<Long> nuoviTurnoIds) {

        RichiestaAdozione richiesta = richiestaAdozioneRepository.findById(richiestaId).orElse(null);
        if (richiesta == null) {
            throw new IllegalArgumentException("Richiesta non trovata");
        }
        if (!richiesta.getAdottante().getUsername().equals(usernameRichiedente)) {
            throw new IllegalStateException("Non puoi modificare una richiesta di un altro utente");
        }
        if (richiesta.getStato() != StatoRichiesta.IN_ATTESA) {
            throw new IllegalStateException("La richiesta è già stata gestita e non può più essere modificata");
        }
        if (nuoviTurnoIds == null || nuoviTurnoIds.isEmpty()) {
            throw new IllegalStateException("Devi selezionare necessariamente un turno per effettuare la visita");
        }

        List<Turno> turniAttuali = turnoRepository.findByRichiestaAdozione(richiesta);
        Set<Long> idAttuali = new HashSet<>();
        for (Turno turno : turniAttuali) {
            idAttuali.add(turno.getId());
        }
        Set<Long> idNuovi = new HashSet<>(nuoviTurnoIds);

        // Libera i turni tenuti finora ma non più selezionati.
        for (Turno turno : turniAttuali) {
            if (!idNuovi.contains(turno.getId())) {
                turno.setAnimale(null);
                turno.setRichiestaAdozione(null);
                turnoRepository.save(turno);
            }
        }

        // Le altre visite già prenotate per lo stesso animale, escludendo quelle di
        // questa stessa richiesta (che vengono comunque sostituite/confermate qui).
        Animale animale = richiesta.getAnimale();
        List<Turno> turniGiaPrenotatiPerAltri = new ArrayList<>();
        for (Turno turno : turnoRepository.findByAnimale(animale)) {
            if (!idAttuali.contains(turno.getId())) {
                turniGiaPrenotatiPerAltri.add(turno);
            }
        }

        LocalDate oggi = LocalDate.now();
        List<Turno> turniDaPrenotare = new ArrayList<>();
        for (Long turnoId : nuoviTurnoIds) {
            if (idAttuali.contains(turnoId)) {
                continue; // già prenotato per questa richiesta: nessuna modifica necessaria
            }
            Turno turno = turnoRepository.findById(turnoId).orElse(null);
            if (turno == null) {
                throw new IllegalArgumentException("Uno dei turni scelti non esiste più");
            }
            if (turno.getAnimale() != null || turno.getRichiestaAdozione() != null) {
                throw new IllegalStateException(
                        "Uno dei turni scelti non è più disponibile: aggiorna la pagina e riprova");
            }
            if (turno.getData().isBefore(oggi)) {
                throw new IllegalStateException("Uno dei turni scelti è ormai passato: aggiorna la pagina e riprova");
            }
            if (sovrappostoPerAnimale(turno, turniGiaPrenotatiPerAltri)
                    || sovrappostoPerAnimale(turno, turniDaPrenotare)) {
                throw new IllegalStateException(
                        "Uno dei turni scelti si sovrappone a un'altra visita già prenotata per questo animale: aggiorna la pagina e riprova");
            }
            turniDaPrenotare.add(turno);
        }

        for (Turno turno : turniDaPrenotare) {
            turno.setAnimale(animale);
            turno.setRichiestaAdozione(richiesta);
            turnoRepository.save(turno);
        }

        return richiesta;
    }

    /** Libera i turni prenotati per una richiesta (usata quando la richiesta viene
     *  rifiutata o cancellata), cosi' tornano disponibili per altri adottanti. */
    @Transactional
    public void liberaTurniPrenotati(RichiestaAdozione richiesta) {
        for (Turno turno : turnoRepository.findByRichiestaAdozione(richiesta)) {
            turno.setAnimale(null);
            turno.setRichiestaAdozione(null);
            turnoRepository.save(turno);
        }
    }

    /**
     * Caso d'uso transazionale principale: approvazione di una richiesta di adozione.
     * Coinvolge due entità e piu' repository in un'unica operazione atomica:
     * 1) la richiesta approvata passa a APPROVATA
     * 2) l'animale passa ad ADOTTATO
     * 3) tutte le altre richieste IN_ATTESA per lo stesso animale vengono rifiutate
     *    automaticamente, liberando i turni che avevano eventualmente prenotato
     * Se un passaggio fallisse a metà, @Transactional garantisce il rollback di tutto.
     */
    @Transactional
    public RichiestaAdozione approvaRichiesta(Long richiestaId) {

        RichiestaAdozione richiesta = richiestaAdozioneRepository.findById(richiestaId).orElse(null);
        if (richiesta == null) {
            throw new IllegalArgumentException("Richiesta non trovata");
        }
        if (richiesta.getStato() != StatoRichiesta.IN_ATTESA) {
            throw new IllegalStateException("La richiesta è già stata gestita");
        }

        Animale animale = richiesta.getAnimale();
        if (animale.getStato() != StatoAnimale.DISPONIBILE) {
            throw new IllegalStateException("L'animale non è più disponibile per l'adozione");
        }

        List<RichiestaAdozione> altreRichieste = richiestaAdozioneRepository
                .findByAnimaleAndStato(animale, StatoRichiesta.IN_ATTESA);

        richiesta.setStato(StatoRichiesta.APPROVATA);
        animale.setStato(StatoAnimale.ADOTTATO);

        for (RichiestaAdozione altra : altreRichieste) {
            if (!altra.getId().equals(richiesta.getId())) {
                altra.setStato(StatoRichiesta.RIFIUTATA);
                liberaTurniPrenotati(altra);
            }
        }

        return richiesta;
    }

    @Transactional
    public RichiestaAdozione rifiutaRichiesta(Long richiestaId) {

        RichiestaAdozione richiesta = richiestaAdozioneRepository.findById(richiestaId).orElse(null);
        if (richiesta == null) {
            throw new IllegalArgumentException("Richiesta non trovata");
        }
        if (richiesta.getStato() != StatoRichiesta.IN_ATTESA) {
            throw new IllegalStateException("La richiesta è già stata gestita");
        }

        richiesta.setStato(StatoRichiesta.RIFIUTATA);
        liberaTurniPrenotati(richiesta);
        return richiesta;
    }

    /**
     * Cancella una richiesta, solo se appartiene all'utente che la sta cancellando.
     */
    @Transactional
    public void eliminaRichiesta(Long richiestaId, String usernameRichiedente) {

        RichiestaAdozione richiesta = richiestaAdozioneRepository.findById(richiestaId).orElse(null);
        if (richiesta == null) {
            throw new IllegalArgumentException("Richiesta non trovata");
        }
        if (!richiesta.getAdottante().getUsername().equals(usernameRichiedente)) {
            throw new IllegalStateException("Non puoi cancellare una richiesta di un altro utente");
        }

        liberaTurniPrenotati(richiesta);
        richiestaAdozioneRepository.deleteById(richiestaId);
    }
}
