package it.uniroma3.siw.progettopersonale.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import it.uniroma3.siw.progettopersonale.service.RichiestaAdozioneService;

/**
 * Valutazione delle richieste di adozione, riservata all'ADMIN.
 */
@Controller
public class AdminRichiestaController {

    private final RichiestaAdozioneService richiestaAdozioneService;

    public AdminRichiestaController(RichiestaAdozioneService richiestaAdozioneService) {
        this.richiestaAdozioneService = richiestaAdozioneService;
    }

    @GetMapping("/admin/richieste")
    public String elenco(@RequestParam(value = "cerca", required = false) String cerca, Model model) {
        model.addAttribute("richiestaList", richiestaAdozioneService.findInAttesaBySearch(cerca));
        model.addAttribute("cerca", cerca != null ? cerca : "");
        return "admin/richieste";
    }

    @PostMapping("/admin/richieste/{id}/approva")
    public String approva(@PathVariable("id") Long id) {
        richiestaAdozioneService.approvaRichiesta(id);
        return "redirect:/admin/richieste";
    }

    @PostMapping("/admin/richieste/{id}/rifiuta")
    public String rifiuta(@PathVariable("id") Long id) {
        richiestaAdozioneService.rifiutaRichiesta(id);
        return "redirect:/admin/richieste";
    }
}
