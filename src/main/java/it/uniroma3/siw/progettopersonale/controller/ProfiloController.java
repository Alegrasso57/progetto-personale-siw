package it.uniroma3.siw.progettopersonale.controller;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
        Utente utente = utenteService.findByUsername(principal.getName());
        model.addAttribute("utente", utente);
        model.addAttribute("errore", null);
        return "profiloForm";
    }

    @PostMapping("/profilo")
    public String salva(@ModelAttribute("nome") String nome,
                         @ModelAttribute("cognome") String cognome,
                         @ModelAttribute("email") String email,
                         @ModelAttribute("telefono") String telefono,
                         Principal principal,
                         Model model) {
        Utente utente = utenteService.findByUsername(principal.getName());
        try {
            utenteService.aggiornaProfilo(utente.getId(), nome, cognome, email, telefono);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("utente", utente);
            model.addAttribute("errore", e.getMessage());
            return "profiloForm";
        }
    }
}
