package it.uniroma3.siw.progettopersonale.controller;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.service.AnimaleService;
import it.uniroma3.siw.progettopersonale.service.RichiestaAdozioneService;
import it.uniroma3.siw.progettopersonale.service.UtenteService;

@Controller
public class RichiestaAdozioneController {

    private final RichiestaAdozioneService richiestaAdozioneService;
    private final AnimaleService animaleService;
    private final UtenteService utenteService;

    public RichiestaAdozioneController(RichiestaAdozioneService richiestaAdozioneService,
                                        AnimaleService animaleService,
                                        UtenteService utenteService) {
        this.richiestaAdozioneService = richiestaAdozioneService;
        this.animaleService = animaleService;
        this.utenteService = utenteService;
    }

    @GetMapping("/animali/{id}/richiedi-adozione")
    public String formRichiesta(@PathVariable("id") Long id, Model model) {
        Animale animale = animaleService.findById(id);
        if (animale == null) {
            return "redirect:/animali";
        }
        model.addAttribute("animale", animale);
        model.addAttribute("errore", null);
        return "richiediAdozione";
    }

    @PostMapping("/animali/{id}/richiedi-adozione")
    public String inviaRichiesta(@PathVariable("id") Long id,
                                  @ModelAttribute("motivazione") String motivazione,
                                  Principal principal,
                                  Model model) {
        Utente adottante = utenteService.findByUsername(principal.getName());
        try {
            richiestaAdozioneService.creaRichiesta(id, adottante.getId(), motivazione);
            return "redirect:/le-mie-richieste";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("animale", animaleService.findById(id));
            model.addAttribute("errore", e.getMessage());
            return "richiediAdozione";
        }
    }

    @GetMapping("/le-mie-richieste")
    public String leMieRichieste(Principal principal, Model model) {
        Utente adottante = utenteService.findByUsername(principal.getName());
        model.addAttribute("richieste", richiestaAdozioneService.findByAdottanteId(adottante.getId()));
        return "leMieRichieste";
    }

    @PostMapping("/le-mie-richieste/{id}/elimina")
    public String eliminaRichiesta(@PathVariable("id") Long id, Principal principal) {
        richiestaAdozioneService.eliminaRichiesta(id, principal.getName());
        return "redirect:/le-mie-richieste";
    }
}