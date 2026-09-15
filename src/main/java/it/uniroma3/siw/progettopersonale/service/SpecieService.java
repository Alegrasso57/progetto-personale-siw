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

    /** Specie presenti tra gli animali disponibili. */
    @Transactional(readOnly = true)
    public List<String> findDisponibili() {
        return specieRepository.findDistinctByStato(StatoAnimale.DISPONIBILE);
    }
}
