package it.uniroma3.siw.progettopersonale.controller;

import java.security.Principal;
import jakarta.validation.Valid;
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
 * i propri dati anagrafici (nome, cognome, email, telefono).
 */
@Controller
public class ProfiloController {

    private final UtenteService utenteService;

    public ProfiloController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    @GetMapping("/profilo/modifica")
    public String formModifica(Principal principal, Model model) {
        model.addAttribute("utente", utenteService.findByUsername(principal.getName()));
        model.addAttribute("username", principal.getName());
        return "profiloForm";
    }

    @PostMapping("/profilo")
    public String salva(@Valid @ModelAttribute("utente") Utente utenteForm,
                         BindingResult bindingResult,
                         Principal principal,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("username", principal.getName());
            return "profiloForm";
        }

        Utente utenteAutenticato = utenteService.findByUsername(principal.getName());
        utenteService.aggiornaProfilo(utenteAutenticato.getId(), utenteForm);
        return "redirect:/";
    }
}
