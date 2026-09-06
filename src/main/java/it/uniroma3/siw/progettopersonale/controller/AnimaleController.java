package it.uniroma3.siw.progettopersonale.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public String elenco(Model model) {
        model.addAttribute("animaleList", animaleService.findDisponibili());
        return "animali";
    }

    @GetMapping("/animali/{id}")
    public String dettaglio(@PathVariable("id") Long id, Model model) {
        Animale animale = animaleService.findById(id);
        if (animale == null) {
            return "redirect:/animali";
        }
        model.addAttribute("animale", animale);
        model.addAttribute("turni", turnoService.findByAnimaleId(id));
        model.addAttribute("recensioni", recensioneService.findByAnimaleId(id));
        return "animaleDetail";
    }
}