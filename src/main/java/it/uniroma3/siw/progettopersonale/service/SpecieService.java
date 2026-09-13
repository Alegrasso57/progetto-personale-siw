package it.uniroma3.siw.progettopersonale.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.progettopersonale.model.StatoAnimale;
import it.uniroma3.siw.progettopersonale.repository.SpecieRepository;

/** Specie di animali trattate dal rifugio: dedotte dagli animali disponibili. */
@Service
public class SpecieService {

    private final SpecieRepository specieRepository;

    public SpecieService(SpecieRepository specieRepository) {
        this.specieRepository = specieRepository;
    }

    /** Specie presenti tra gli animali disponibili, in ordine alfabetico crescente (A-Z). */
    @Transactional(readOnly = true)
    public List<String> findDisponibili() {
        return specieRepository.findDistinctByStatoOrderByNomeAsc(StatoAnimale.DISPONIBILE);
    }

    /**
     * Stesso elenco, con il criterio di ordinamento scelto dalla richiesta
     * (vedi SpecieController, che riceve "ordina" come @RequestParam e lo
     * passa qui). Criteri validi: "nome" (crescente, A-Z, default),
     * "nome-desc" (decrescente, Z-A). Per aggiungerne un altro basta un nuovo
     * "case" qui, con la query corrispondente in SpecieRepository.
     */
    @Transactional(readOnly = true)
    public List<String> findDisponibili(String criterio) {
        return switch (criterio == null ? "" : criterio) {
            case "nome-desc" -> specieRepository.findDistinctByStatoOrderByNomeDesc(StatoAnimale.DISPONIBILE);
            default -> findDisponibili();
        };
    }
}
