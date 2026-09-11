package it.uniroma3.siw.progettopersonale.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import it.uniroma3.siw.progettopersonale.dto.AnimaleRicercaDTO;
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
        List<Animale> animaleList;
        if (specie != null && !specie.isBlank()) {
            animaleList = animaleService.findDisponibiliBySpecie(specie);
            model.addAttribute("specie", specie);
        } else {
            animaleList = animaleService.findDisponibiliBySearch(cerca);
        }
        model.addAttribute("animaleList", animaleList);
        model.addAttribute("cerca", cerca != null ? cerca : "");
        // Stessa lista, ma solo i campi che servono alla barra di ricerca in React
        // (vedi frontend-ricerca-animali/): la pagina la usa come risultati iniziali,
        // cosi' non deve fare una chiamata in piu' al caricamento.
        model.addAttribute("animaleListJson", animaleList.stream().map(AnimaleRicercaDTO::from).toList());
        return "animali";
    }

    /**
     * Stessa ricerca di /animali (solo per nome/specie, sugli animali
     * disponibili), in JSON: usata dalla barra di ricerca in React per
     * aggiornare i risultati mentre si scrive, senza ricaricare la pagina.
     */
    @GetMapping("/api/animali/ricerca")
    @ResponseBody
    public List<AnimaleRicercaDTO> ricercaJson(@RequestParam(value = "cerca", required = false) String cerca) {
        return animaleService.findDisponibiliBySearch(cerca).stream().map(AnimaleRicercaDTO::from).toList();
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
