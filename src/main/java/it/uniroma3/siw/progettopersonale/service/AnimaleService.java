package it.uniroma3.siw.progettopersonale.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.StatoAnimale;
import it.uniroma3.siw.progettopersonale.repository.AnimaleRepository;
import it.uniroma3.siw.progettopersonale.repository.RecensioneRepository;
import it.uniroma3.siw.progettopersonale.repository.RichiestaAdozioneRepository;
import it.uniroma3.siw.progettopersonale.repository.TurnoRepository;

@Service
public class AnimaleService {

    private final AnimaleRepository animaleRepository;
    private final TurnoRepository turnoRepository;
    private final RichiestaAdozioneRepository richiestaAdozioneRepository;
    private final RecensioneRepository recensioneRepository;

    public AnimaleService(AnimaleRepository animaleRepository,
                           TurnoRepository turnoRepository,
                           RichiestaAdozioneRepository richiestaAdozioneRepository,
                           RecensioneRepository recensioneRepository) {
        this.animaleRepository = animaleRepository;
        this.turnoRepository = turnoRepository;
        this.richiestaAdozioneRepository = richiestaAdozioneRepository;
        this.recensioneRepository = recensioneRepository;
    }

    @Transactional(readOnly = true)
    public List<Animale> findAll() {
        return animaleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Animale> findDisponibili() {
        return animaleRepository.findByStato(StatoAnimale.DISPONIBILE);
    }

    /** Ricerca full-text su nome o specie tra gli animali disponibili. */
    @Transactional(readOnly = true)
    public List<Animale> findDisponibiliBySearch(String q) {
        if (q == null || q.isBlank()) {
            return findDisponibili();
        }
        return animaleRepository.searchByNomeOrSpecieAndStato(q.trim(), StatoAnimale.DISPONIBILE);
    }

    /** Ricerca full-text su nome o specie su tutti gli animali (per il volontario). */
    @Transactional(readOnly = true)
    public List<Animale> findAllBySearch(String q) {
        if (q == null || q.isBlank()) {
            return findAll();
        }
        return animaleRepository.findByNomeContainingIgnoreCaseOrSpecieContainingIgnoreCase(q.trim(), q.trim());
    }

    /**
     * Carica tutti gli animali con le recensioni in una sola query (soluzione N+1).
     * Usato nella pagina admin per dimostrare JOIN FETCH.
     */
    @Transactional(readOnly = true)
    public List<Animale> findAllWithRecensioni() {
        return animaleRepository.findAllWithRecensioni();
    }

    @Transactional(readOnly = true)
    public Animale findById(Long id) {
        return animaleRepository.findById(id).orElse(null);
    }

    @Transactional
    public Animale save(Animale animale) {
        return animaleRepository.save(animale);
    }

    /**
     * Elimina un animale insieme a tutti i turni, le richieste di adozione e le recensioni collegate.
     * Senza questa pulizia esplicita l'eliminazione fallirebbe per vincolo di integrità referenziale
     * (Whitelabel Error Page) non appena l'animale avesse anche un solo record collegato.
     * L'intera operazione è atomica grazie a @Transactional: se qualcosa fallisce a metà, tutto torna indietro.
     */
    @Transactional
    public void deleteById(Long id) {

        Animale animale = animaleRepository.findById(id).orElse(null);
        if (animale == null) {
            throw new IllegalArgumentException("Animale non trovato");
        }

        turnoRepository.deleteAll(turnoRepository.findByAnimale(animale));
        richiestaAdozioneRepository.deleteAll(richiestaAdozioneRepository.findByAnimale(animale));
        recensioneRepository.deleteAll(recensioneRepository.findByAnimale(animale));

        animaleRepository.deleteById(id);
    }
}