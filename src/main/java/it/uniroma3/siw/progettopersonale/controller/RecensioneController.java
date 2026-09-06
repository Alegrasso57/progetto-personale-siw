package it.uniroma3.siw.progettopersonale.controller;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.Recensione;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.service.AnimaleService;
import it.uniroma3.siw.progettopersonale.service.RecensioneService;
import it.uniroma3.siw.progettopersonale.service.UtenteService;

@Controller
public class RecensioneController {

    private final RecensioneService recensioneService;
    private final AnimaleService animaleService;
    private final UtenteService utenteService;

    public RecensioneController(RecensioneService recensioneService,
                                 AnimaleService animaleService,
                                 UtenteService utenteService) {
        this.recensioneService = recensioneService;
        this.animaleService = animaleService;
        this.utenteService = utenteService;
    }

    @GetMapping("/animali/{id}/recensioni/nuova")
    public String formNuova(@PathVariable("id") Long id, Model model) {
        Animale animale = animaleService.findById(id);
        if (animale == null) {
            return "redirect:/animali";
        }
        model.addAttribute("animale", animale);
        model.addAttribute("recensione", new Recensione());
        model.addAttribute("errore", null);
        return "recensioneForm";
    }

    @PostMapping("/animali/{id}/recensioni")
    public String creaRecensione(@PathVariable("id") Long id,
                                  @ModelAttribute("testo") String testo,
                                  @ModelAttribute("voto") Integer voto,
                                  Principal principal,
                                  Model model) {
        Utente autore = utenteService.findByUsername(principal.getName());
        try {
            recensioneService.creaRecensione(id, autore.getId(), testo, voto);
            return "redirect:/animali/" + id;
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("animale", animaleService.findById(id));
            model.addAttribute("recensione", new Recensione());
            model.addAttribute("errore", e.getMessage());
            return "recensioneForm";
        }
    }

    @GetMapping("/recensioni/{id}/modifica")
    public String formModifica(@PathVariable("id") Long id, Principal principal, Model model) {
        Recensione recensione = recensioneService.findById(id);
        if (recensione == null || !recensione.getAutore().getUsername().equals(principal.getName())) {
            return "redirect:/animali";
        }
        model.addAttribute("animale", recensione.getAnimale());
        model.addAttribute("recensione", recensione);
        model.addAttribute("errore", null);
        return "recensioneForm";
    }

    @PostMapping("/recensioni/{id}/modifica")
    public String modificaRecensione(@PathVariable("id") Long id,
                                      @ModelAttribute("testo") String testo,
                                      @ModelAttribute("voto") Integer voto,
                                      Principal principal,
                                      Model model) {
        try {
            Recensione recensione = recensioneService.modificaRecensione(id, principal.getName(), testo, voto);
            return "redirect:/animali/" + recensione.getAnimale().getId();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Recensione recensione = recensioneService.findById(id);
            model.addAttribute("animale", recensione != null ? recensione.getAnimale() : null);
            model.addAttribute("recensione", recensione);
            model.addAttribute("errore", e.getMessage());
            return "recensioneForm";
        }
    }

    @PostMapping("/recensioni/{id}/elimina")
    public String eliminaRecensione(@PathVariable("id") Long id, Principal principal) {
        Recensione recensione = recensioneService.findById(id);
        Long animaleId = (recensione != null) ? recensione.getAnimale().getId() : null;
        recensioneService.eliminaRecensione(id, principal.getName());
        return "redirect:/animali/" + (animaleId != null ? animaleId : "");
    }
}