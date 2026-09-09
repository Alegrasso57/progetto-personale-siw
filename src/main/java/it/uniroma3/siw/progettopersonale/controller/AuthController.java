package it.uniroma3.siw.progettopersonale.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import it.uniroma3.siw.progettopersonale.exception.CodiceVolontarioNonValidoException;
import it.uniroma3.siw.progettopersonale.exception.UsernameGiaUtilizzatoException;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.service.CredenzialiService;

@Controller
public class AuthController {

    private final CredenzialiService credenzialiService;

    public AuthController(CredenzialiService credenzialiService) {
        this.credenzialiService = credenzialiService;
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
                                  BindingResult bindingResult,
                                  Model model) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        Utente utente = new Utente();
        utente.setNome(form.getNome());
        utente.setCognome(form.getCognome());

        try {
            credenzialiService.registra(form.getUsername(), form.getPassword(), form.getRuolo(),
                    form.getCodiceVolontario(), utente);
            return "redirect:/login";
        } catch (UsernameGiaUtilizzatoException | CodiceVolontarioNonValidoException e) {
            bindingResult.reject("erroreRegistrazione", e.getMessage());
            return "register";
        }
    }
}
