package it.uniroma3.siw.progettopersonale.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import it.uniroma3.siw.progettopersonale.service.SpecieService;

/**
 * Pagina dedicata alle specie trattate dal sito (diversa dall'elenco animali):
 * separata insieme a SpecieService/SpecieRepository, cosi' quello che riguarda
 * le specie si implementa qui senza toccare il codice degli Animale.
 */
@Controller
public class SpecieController {

    private final SpecieService specieService;

    public SpecieController(SpecieService specieService) {
        this.specieService = specieService;
    }

    @GetMapping("/specie")
    public String elenco(Model model) {
        model.addAttribute("specieDisponibili", specieService.findDisponibili());
        return "specie";
    }
}
