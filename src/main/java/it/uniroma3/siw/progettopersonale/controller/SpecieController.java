package it.uniroma3.siw.progettopersonale.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.progettopersonale.service.SpecieService;

/**
 * Pagina dedicata alle specie trattate dal sito (diversa dall'elenco
 * animali): prima viveva dentro AnimaleController, ora e' separata insieme a
 * SpecieService/SpecieRepository, cosi' l'ordinamento specifico di questa
 * pagina si implementa qui senza toccare il codice degli Animale.
 */
@Controller
public class SpecieController {

    private final SpecieService specieService;

    public SpecieController(SpecieService specieService) {
        this.specieService = specieService;
    }

    @GetMapping("/specie")
    public String elenco(@RequestParam(value = "ordina", required = false) String ordina, Model model) {
        List<String> specieList = specieService.findDisponibili(ordina);
        // Sovrascrive, solo per questa richiesta, l'attributo "specieDisponibili"
        // gia' presente in ogni pagina via GlobalController: le altre pagine
        // (es. il menu a tendina nella nav bar) continuano a vedere l'elenco
        // di base, non ordinato secondo "ordina".
        model.addAttribute("specieDisponibili", specieList);
        model.addAttribute("ordina", ordina != null ? ordina : "");
        return "specie";
    }
}
