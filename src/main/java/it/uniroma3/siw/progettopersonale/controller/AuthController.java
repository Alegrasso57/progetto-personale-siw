package it.uniroma3.siw.progettopersonale.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import it.uniroma3.siw.progettopersonale.model.Ruolo;
import it.uniroma3.siw.progettopersonale.service.UtenteService;

@Controller
public class AuthController {

    private final UtenteService utenteService;

    public AuthController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    @GetMapping("/login")
    public String mostraLogin() {
        return "login";
    }

    @GetMapping("/register")
    public String mostraRegistrazione(Model model) {
        model.addAttribute("errore", null);
        return "register";
    }

    @PostMapping("/register")
    public String registraUtente(@ModelAttribute("username") String username,
                                  @ModelAttribute("password") String password,
                                  @ModelAttribute("nome") String nome,
                                  @ModelAttribute("cognome") String cognome,
                                  @ModelAttribute("ruolo") Ruolo ruolo,
                                  @ModelAttribute("codiceVolontario") String codiceVolontario,
                                  Model model) {
        try {
            utenteService.registra(username, password, nome, cognome, ruolo, codiceVolontario);
            return "redirect:/login";
        } catch (IllegalStateException e) {
            model.addAttribute("errore", e.getMessage());
            return "register";
        }
    }
}