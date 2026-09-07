package it.uniroma3.siw.progettopersonale.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import it.uniroma3.siw.progettopersonale.service.UtenteService;

@Controller
public class VolontarioController {

    private final UtenteService utenteService;

    public VolontarioController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    @GetMapping("/volontari")
    public String elenco(@RequestParam(value = "cerca", required = false) String cerca, Model model) {
        model.addAttribute("volontarioList", utenteService.findVolontariBySearch(cerca));
        model.addAttribute("cerca", cerca != null ? cerca : "");
        return "volontari";
    }
}