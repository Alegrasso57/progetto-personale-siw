package it.uniroma3.siw.progettopersonale.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import it.uniroma3.siw.progettopersonale.exception.AccessoNonAutorizzatoException;
import it.uniroma3.siw.progettopersonale.exception.RecensioneGiaPresenteException;
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
        model.addAttribute("animale", animaleService.findById(id));
        model.addAttribute("recensione", new Recensione());
        return "recensioneForm";
    }

    @PostMapping("/animali/{id}/recensioni")
    public String creaRecensione(@PathVariable("id") Long id,
                                  @Valid @ModelAttribute("recensione") Recensione recensioneForm,
                                  BindingResult bindingResult,
                                  Model model) {

        Animale animale = animaleService.findById(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("animale", animale);
            return "recensioneForm";
        }

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Utente autore = utenteService.findByUsername(userDetails.getUsername());
        try {
            recensioneService.creaRecensione(id, autore.getId(), recensioneForm);
            return "redirect:/animali/" + id;
        } catch (RecensioneGiaPresenteException e) {
            model.addAttribute("animale", animale);
            bindingResult.reject("recensioneGiaPresente", e.getMessage());
            return "recensioneForm";
        }
    }

    @GetMapping("/recensioni/{id}/modifica")
    public String formModifica(@PathVariable("id") Long id, Model model) {
        Recensione recensione = recensioneService.findById(id);
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Utente autoreAutenticato = utenteService.findByUsername(userDetails.getUsername());
        if (!recensione.getAutore().getId().equals(autoreAutenticato.getId())) {
            throw new AccessoNonAutorizzatoException("Non puoi modificare la recensione di un altro utente.");
        }
        model.addAttribute("animale", recensione.getAnimale());
        model.addAttribute("recensione", recensione);
        return "recensioneForm";
    }

    @PostMapping("/recensioni/{id}/modifica")
    public String modificaRecensione(@PathVariable("id") Long id,
                                      @Valid @ModelAttribute("recensione") Recensione recensioneForm,
                                      BindingResult bindingResult,
                                      Model model) {

        Recensione recensioneEsistente = recensioneService.findById(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("animale", recensioneEsistente.getAnimale());
            return "recensioneForm";
        }

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Utente autoreAutenticato = utenteService.findByUsername(userDetails.getUsername());
        Recensione recensione = recensioneService.modificaRecensione(id, autoreAutenticato.getId(), recensioneForm);
        return "redirect:/animali/" + recensione.getAnimale().getId();
    }

    @PostMapping("/recensioni/{id}/elimina")
    public String eliminaRecensione(@PathVariable("id") Long id) {
        Recensione recensione = recensioneService.findById(id);
        Long animaleId = recensione.getAnimale().getId();
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Utente autoreAutenticato = utenteService.findByUsername(userDetails.getUsername());
        recensioneService.eliminaRecensione(id, autoreAutenticato.getId());
        return "redirect:/animali/" + animaleId;
    }
}
