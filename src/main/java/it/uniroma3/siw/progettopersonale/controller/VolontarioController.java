package it.uniroma3.siw.progettopersonale.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import it.uniroma3.siw.progettopersonale.service.UtenteService;

@Controller
public class VolontarioController {

    private final UtenteService utenteService;

    public VolontarioController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    @GetMapping("/volontari")
    public String elenco(Model model) {
        model.addAttribute("volontarioList", utenteService.findVolontari());
        return "volontari";
    }
}