package it.uniroma3.siw.progettopersonale.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import it.uniroma3.siw.progettopersonale.service.RichiestaAdozioneService;

@Controller
public class VolontarioRichiestaController {

    private final RichiestaAdozioneService richiestaAdozioneService;

    public VolontarioRichiestaController(RichiestaAdozioneService richiestaAdozioneService) {
        this.richiestaAdozioneService = richiestaAdozioneService;
    }

    @GetMapping("/volontario/richieste")
    public String elenco(Model model) {
        model.addAttribute("richiestaList", richiestaAdozioneService.findInAttesa());
        return "volontario/richieste";
    }

    @PostMapping("/volontario/richieste/{id}/approva")
    public String approva(@PathVariable("id") Long id) {
        richiestaAdozioneService.approvaRichiesta(id);
        return "redirect:/volontario/richieste";
    }

    @PostMapping("/volontario/richieste/{id}/rifiuta")
    public String rifiuta(@PathVariable("id") Long id) {
        richiestaAdozioneService.rifiutaRichiesta(id);
        return "redirect:/volontario/richieste";
    }
}