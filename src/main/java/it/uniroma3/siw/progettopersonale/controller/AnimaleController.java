package it.uniroma3.siw.progettopersonale.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.service.AnimaleService;
import it.uniroma3.siw.progettopersonale.service.RecensioneService;
import it.uniroma3.siw.progettopersonale.service.TurnoService;

@Controller
public class AnimaleController {

    private final AnimaleService animaleService;
    private final TurnoService turnoService;
    private final RecensioneService recensioneService;

    public AnimaleController(AnimaleService animaleService,
                              TurnoService turnoService,
                              RecensioneService recensioneService) {
        this.animaleService = animaleService;
        this.turnoService = turnoService;
        this.recensioneService = recensioneService;
    }

    @GetMapping("/animali")
    public String elenco(@RequestParam(value = "cerca", required = false) String cerca,
                          @RequestParam(value = "specie", required = false) String specie,
                          Model model) {
        if (specie != null && !specie.isBlank()) {
            model.addAttribute("animaleList", animaleService.findDisponibiliBySpecie(specie));
        } else {
            model.addAttribute("animaleList", animaleService.findDisponibiliBySearch(cerca));
        }
        model.addAttribute("cerca", cerca != null ? cerca : "");
        return "animali";
    }

    /**
     * Pagina dedicata alle specie trattate dal sito (diversa dall'elenco
     * animali): specieDisponibili e' gia' nel Model per ogni pagina, via il
     * @ModelAttribute di GlobalController.
     */
    @GetMapping("/specie")
    public String elencoSpecie() {
        return "specie";
    }

    @GetMapping("/animali/{id}")
    public String dettaglio(@PathVariable("id") Long id, Model model) {
        Animale animale = animaleService.findById(id);
        model.addAttribute("animale", animale);
        model.addAttribute("turni", turnoService.findByAnimaleId(id));
        model.addAttribute("recensioni", recensioneService.findByAnimaleId(id));
        return "animaleDetail";
    }
}
