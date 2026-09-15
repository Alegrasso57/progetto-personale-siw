package it.uniroma3.siw.progettopersonale.service;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.uniroma3.siw.progettopersonale.exception.AccessoNonAutorizzatoException;
import it.uniroma3.siw.progettopersonale.exception.AnimaleNonDisponibileException;
import it.uniroma3.siw.progettopersonale.exception.AnimaleNonTrovatoException;
import it.uniroma3.siw.progettopersonale.exception.OperazioneNonConsentitaException;
import it.uniroma3.siw.progettopersonale.exception.RichiestaAdozioneNonTrovataException;
import it.uniroma3.siw.progettopersonale.exception.RichiestaGiaPresenteException;
import it.uniroma3.siw.progettopersonale.exception.UtenteNonTrovatoException;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.RichiestaAdozione;
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

    private static final Logger logger = LoggerFactory.getLogger(RichiestaAdozioneService.class);

    private final RichiestaAdozioneRepository richiestaAdozioneRepository;
    private final AnimaleRepository animaleRepository;
    private final UtenteRepository utenteRepository;
    private final TurnoRepository turnoRepository;
    private final TurnoService turnoService;

    public RichiestaAdozioneService(RichiestaAdozioneRepository richiestaAdozioneRepository,
                                     AnimaleRepository animaleRepository,
                                     UtenteRepository utenteRepository,
                                     TurnoRepository turnoRepository,
                                     TurnoService turnoService) {
        this.richiestaAdozioneRepository = richiestaAdozioneRepository;
        this.animaleRepository = animaleRepository;
        this.utenteRepository = utenteRepository;
        this.turnoRepository = turnoRepository;
        this.turnoService = turnoService;
    }

    @Transactional(readOnly = true)
    public RichiestaAdozione findById(Long id) {
        return richiestaAdozioneRepository.findById(id)
                .orElseThrow(() -> new RichiestaAdozioneNonTrovataException(id));
    }

    @Transactional(readOnly = true)
    public List<RichiestaAdozione> findByAdottanteId(Long adottanteId) {
        Utente adottante = utenteRepository.findById(adottanteId)
                .orElseThrow(() -> new UtenteNonTrovatoException(adottanteId));
        return richiestaAdozioneRepository.findByAdottante(adottante);
    }

    /** Richieste ancora da valutare. */
    @Transactional(readOnly = true)
    public List<RichiestaAdozione> findInAttesa() {
        return richiestaAdozioneRepository.findByStato(StatoRichiesta.IN_ATTESA);
    }

    /** Filtra le richieste in attesa per nome animale o nome/cognome dell'utente. */
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

    /**
     * Vero se questo utente ha gia' una richiesta ancora IN_ATTESA per questo
     * animale. Serve alle pagine per non proporre il pulsante "Richiedi
     * l'adozione" una seconda volta: la regola vera resta comunque dentro
     * creaRichiesta, che rifiuta il doppione anche se qualcuno arrivasse
     * all'URL direttamente.
     */
    @Transactional(readOnly = true)
    public boolean haRichiestaInAttesa(Long animaleId, Long adottanteId) {
        Animale animale = animaleRepository.findById(animaleId).orElse(null);
        Utente adottante = utenteRepository.findById(adottanteId).orElse(null);
        if (animale == null || adottante == null) {
            return false;
        }
        return richiestaAdozioneRepository
                .findByAdottanteAndAnimaleAndStato(adottante, animale, StatoRichiesta.IN_ATTESA)
                .isPresent();
    }

    /** Filtra le richieste di un utente per nome animale. */
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

    /**
     * Un utente invia una richiesta di adozione per un animale disponibile,
     * scegliendo uno degli slot orari messi a disposizione dall'admin.
     *
     * Regole di business:
     *   - l'animale deve essere DISPONIBILE
     *   - l'utente non deve avere gia' una richiesta IN_ATTESA per quell'animale
     *   - lo slot va scelto e deve essere ancora libero (lo verifica TurnoService)
     */
    @Transactional
    public RichiestaAdozione creaRichiesta(Long animaleId, Long adottanteId, String motivazione, Long turnoId) {

        Animale animale = animaleRepository.findById(animaleId)
                .orElseThrow(() -> new AnimaleNonTrovatoException(animaleId));
        if (animale.getStato() != StatoAnimale.DISPONIBILE) {
            throw new AnimaleNonDisponibileException();
        }

        Utente adottante = utenteRepository.findById(adottanteId)
                .orElseThrow(() -> new UtenteNonTrovatoException(adottanteId));

        boolean giaRichiesta = richiestaAdozioneRepository
                .findByAdottanteAndAnimaleAndStato(adottante, animale, StatoRichiesta.IN_ATTESA)
                .isPresent();
        if (giaRichiesta) {
            throw new RichiestaGiaPresenteException();
        }

        if (turnoId == null) {
            throw new OperazioneNonConsentitaException("Devi scegliere uno slot orario per la visita.");
        }

        RichiestaAdozione richiesta = new RichiestaAdozione();
        richiesta.setAnimale(animale);
        richiesta.setAdottante(adottante);
        richiesta.setMotivazione(motivazione);
        richiesta.setDataRichiesta(LocalDate.now());
        richiesta.setStato(StatoRichiesta.IN_ATTESA);
        richiesta = richiestaAdozioneRepository.save(richiesta);

        // Prenota lo slot: se nel frattempo qualcun altro l'ha preso, il service
        // dei turni lancia un'eccezione e @Transactional annulla anche la richiesta.
        turnoService.prenota(turnoId, richiesta);

        logger.info("Richiesta di adozione creata: animaleId={}, adottanteId={}, turnoId={}",
                animaleId, adottanteId, turnoId);
        return richiesta;
    }

    /**
     * L'utente modifica una PROPRIA richiesta ancora IN_ATTESA: puo' cambiare la
     * motivazione e lo slot orario scelto.
     *
     * Se lo slot cambia, prima si prenota il nuovo e poi si libera il vecchio:
     * in quest'ordine, se il nuovo non fosse piu' disponibile l'eccezione fa
     * rollback di tutto e l'utente si ritrova la richiesta intatta com'era.
     */
    @Transactional
    public RichiestaAdozione modificaRichiesta(Long richiestaId, Long adottanteAutenticatoId,
                                                String motivazione, Long nuovoTurnoId) {

        RichiestaAdozione richiesta = findById(richiestaId);

        if (!richiesta.getAdottante().getId().equals(adottanteAutenticatoId)) {
            throw new AccessoNonAutorizzatoException("Non puoi modificare una richiesta di un altro utente.");
        }
        if (richiesta.getStato() != StatoRichiesta.IN_ATTESA) {
            throw new OperazioneNonConsentitaException(
                    "La richiesta è già stata gestita e non può più essere modificata.");
        }
        if (nuovoTurnoId == null) {
            throw new OperazioneNonConsentitaException("Devi scegliere uno slot orario per la visita.");
        }

        richiesta.setMotivazione(motivazione);

        Turno turnoAttuale = turnoRepository.findByRichiestaAdozione(richiesta).orElse(null);
        boolean slotCambiato = turnoAttuale == null || !turnoAttuale.getId().equals(nuovoTurnoId);

        if (slotCambiato) {
            turnoService.prenota(nuovoTurnoId, richiesta);
            if (turnoAttuale != null) {
                turnoAttuale.setRichiestaAdozione(null);
                turnoRepository.save(turnoAttuale);
            }
        }

        logger.info("Richiesta di adozione modificata: id={}, turnoId={}", richiestaId, nuovoTurnoId);
        return richiesta;
    }

    /**
     * Caso d'uso transazionale principale: approvazione di una richiesta.
     * Coinvolge piu' entita' in un'unica operazione atomica:
     *   1) la richiesta approvata passa a APPROVATA
     *   2) l'animale passa ad ADOTTATO
     *   3) tutte le altre richieste IN_ATTESA per lo stesso animale vengono
     *      rifiutate e i loro slot orari tornano liberi
     * Se un passaggio fallisse a meta', @Transactional garantisce il rollback di tutto.
     */
    @Transactional
    public RichiestaAdozione approvaRichiesta(Long richiestaId) {

        RichiestaAdozione richiesta = findById(richiestaId);
        if (richiesta.getStato() != StatoRichiesta.IN_ATTESA) {
            throw new OperazioneNonConsentitaException("La richiesta è già stata gestita.");
        }

        Animale animale = richiesta.getAnimale();
        if (animale.getStato() != StatoAnimale.DISPONIBILE) {
            throw new AnimaleNonDisponibileException();
        }

        List<RichiestaAdozione> altreRichieste = richiestaAdozioneRepository
                .findByAnimaleAndStato(animale, StatoRichiesta.IN_ATTESA);

        richiesta.setStato(StatoRichiesta.APPROVATA);
        animale.setStato(StatoAnimale.ADOTTATO);

        for (RichiestaAdozione altra : altreRichieste) {
            if (!altra.getId().equals(richiesta.getId())) {
                altra.setStato(StatoRichiesta.RIFIUTATA);
                turnoService.liberaTurnoDi(altra);
            }
        }

        logger.info("Richiesta di adozione approvata: id={}", richiestaId);
        return richiesta;
    }

    @Transactional
    public RichiestaAdozione rifiutaRichiesta(Long richiestaId) {

        RichiestaAdozione richiesta = findById(richiestaId);
        if (richiesta.getStato() != StatoRichiesta.IN_ATTESA) {
            throw new OperazioneNonConsentitaException("La richiesta è già stata gestita.");
        }

        richiesta.setStato(StatoRichiesta.RIFIUTATA);
        // Lo slot torna disponibile per altri utenti.
        turnoService.liberaTurnoDi(richiesta);
        logger.info("Richiesta di adozione rifiutata: id={}", richiestaId);
        return richiesta;
    }

    /** Cancella una richiesta, solo se appartiene all'utente che la sta cancellando. */
    @Transactional
    public void eliminaRichiesta(Long richiestaId, Long adottanteAutenticatoId) {

        RichiestaAdozione richiesta = findById(richiestaId);
        if (!richiesta.getAdottante().getId().equals(adottanteAutenticatoId)) {
            throw new AccessoNonAutorizzatoException("Non puoi cancellare una richiesta di un altro utente.");
        }

        // Prima si libera lo slot, altrimenti resterebbe legato a una richiesta cancellata.
        turnoService.liberaTurnoDi(richiesta);
        richiestaAdozioneRepository.deleteById(richiestaId);
        logger.info("Richiesta di adozione eliminata: id={}", richiestaId);
    }
}
