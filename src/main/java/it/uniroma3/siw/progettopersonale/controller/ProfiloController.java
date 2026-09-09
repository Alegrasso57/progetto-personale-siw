package it.uniroma3.siw.progettopersonale.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.service.UtenteService;

/**
 * Consente a qualunque utente autenticato (adottante o volontario) di modificare
 * i propri dati anagrafici (nome, cognome).
 */
@Controller
public class ProfiloController {

    private final UtenteService utenteService;

    public ProfiloController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    @GetMapping("/profilo/modifica")
    public String formModifica(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        model.addAttribute("utente", utenteService.findByUsername(userDetails.getUsername()));
        model.addAttribute("username", userDetails.getUsername());
        return "profiloForm";
    }

    @PostMapping("/profilo")
    public String salva(@Valid @ModelAttribute("utente") Utente utenteForm,
                         BindingResult bindingResult,
                         Model model) {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (bindingResult.hasErrors()) {
            model.addAttribute("username", userDetails.getUsername());
            return "profiloForm";
        }

        Utente utenteAutenticato = utenteService.findByUsername(userDetails.getUsername());
        utenteService.aggiornaProfilo(utenteAutenticato.getId(), utenteForm);
        return "redirect:/";
    }
}
