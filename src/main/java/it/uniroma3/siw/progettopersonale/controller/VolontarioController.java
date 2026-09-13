package it.uniroma3.siw.progettopersonale.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.service.VolontarioService;

@Controller
public class VolontarioController {

    private final VolontarioService volontarioService;

    public VolontarioController(VolontarioService volontarioService) {
        this.volontarioService = volontarioService;
    }

    @GetMapping("/volontari")
    public String elenco(@RequestParam(value = "cerca", required = false) String cerca, Model model) {
        List<Utente> volontarioList = volontarioService.findBySearch(cerca);
        model.addAttribute("volontarioList", volontarioList);
        model.addAttribute("cerca", cerca != null ? cerca : "");
        return "volontari";
    }
}
