package it.uniroma3.siw.progettopersonale.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import it.uniroma3.siw.progettopersonale.exception.UsernameGiaUtilizzatoException;
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
        model.addAttribute("registrazioneForm", new RegistrazioneForm());
        return "register";
    }

    @PostMapping("/register")
    public String registraUtente(@Valid @ModelAttribute("registrazioneForm") RegistrazioneForm form,
                                  BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            utenteService.registra(form.getUsername(), form.getPassword(),
                    form.getNome(), form.getCognome());
            return "redirect:/login";
        } catch (UsernameGiaUtilizzatoException e) {
            bindingResult.reject("erroreRegistrazione", e.getMessage());
            return "register";
        }
    }
}
