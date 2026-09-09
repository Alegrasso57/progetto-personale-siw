package it.uniroma3.siw.progettopersonale.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.uniroma3.siw.progettopersonale.exception.AnimaleNonTrovatoException;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.StatoAnimale;
import it.uniroma3.siw.progettopersonale.repository.AnimaleRepository;

@Service
public class AnimaleService {

    private static final Logger logger = LoggerFactory.getLogger(AnimaleService.class);

    private final AnimaleRepository animaleRepository;

    public AnimaleService(AnimaleRepository animaleRepository) {
        this.animaleRepository = animaleRepository;
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
    public Iterable<Animale> findAllBySearch(String q) {
        if (q == null || q.isBlank()) {
            return animaleRepository.findAll();
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
        return animaleRepository.findById(id).orElseThrow(() -> new AnimaleNonTrovatoException(id));
    }

    @Transactional
    public Animale save(Animale animale) {
        Animale salvato = animaleRepository.save(animale);
        logger.info("Animale salvato: id={}, nome={}", salvato.getId(), salvato.getNome());
        return salvato;
    }

    /**
     * Elimina un animale. Turni, richieste di adozione e recensioni collegate vengono
     * eliminati automaticamente da JPA grazie a cascade = ALL (evento REMOVE)
     * dichiarato sulle rispettive associazioni OneToMany nell'entità Animale: non serve
     * più occuparsene esplicitamente qui.
     */
    @Transactional
    public void deleteById(Long id) {
        Animale animale = findById(id);
        animaleRepository.delete(animale);
        logger.info("Animale eliminato: id={}", id);
    }
}
