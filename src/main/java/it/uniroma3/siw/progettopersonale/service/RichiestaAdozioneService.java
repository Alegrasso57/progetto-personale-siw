package it.uniroma3.siw.progettopersonale.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.RichiestaAdozione;
import it.uniroma3.siw.progettopersonale.model.Ruolo;
import it.uniroma3.siw.progettopersonale.model.StatoAnimale;
import it.uniroma3.siw.progettopersonale.model.StatoRichiesta;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.repository.AnimaleRepository;
import it.uniroma3.siw.progettopersonale.repository.RichiestaAdozioneRepository;
import it.uniroma3.siw.progettopersonale.repository.UtenteRepository;

@Service
public class RichiestaAdozioneService {

    private final RichiestaAdozioneRepository richiestaAdozioneRepository;
    private final AnimaleRepository animaleRepository;
    private final UtenteRepository utenteRepository;

    public RichiestaAdozioneService(RichiestaAdozioneRepository richiestaAdozioneRepository,
                                     AnimaleRepository animaleRepository,
                                     UtenteRepository utenteRepository) {
        this.richiestaAdozioneRepository = richiestaAdozioneRepository;
        this.animaleRepository = animaleRepository;
        this.utenteRepository = utenteRepository;
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
    public RichiestaAdozione creaRichiesta(Long animaleId, Long adottanteId, String motivazione) {

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

        RichiestaAdozione richiesta = new RichiestaAdozione();
        richiesta.setAnimale(animale);
        richiesta.setAdottante(adottante);
        richiesta.setMotivazione(motivazione);
        richiesta.setDataRichiesta(LocalDate.now());
        richiesta.setStato(StatoRichiesta.IN_ATTESA);

        return richiestaAdozioneRepository.save(richiesta);
    }

    /**
     * Caso d'uso transazionale principale: approvazione di una richiesta di adozione.
     * Coinvolge due entità e piu' repository in un'unica operazione atomica:
     * 1) la richiesta approvata passa a APPROVATA
     * 2) l'animale passa ad ADOTTATO
     * 3) tutte le altre richieste IN_ATTESA per lo stesso animale vengono rifiutate automaticamente
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

        richiestaAdozioneRepository.deleteById(richiestaId);
    }
}