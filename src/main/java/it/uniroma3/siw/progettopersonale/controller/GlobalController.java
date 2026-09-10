package it.uniroma3.siw.progettopersonale.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import it.uniroma3.siw.progettopersonale.service.AnimaleService;
import it.uniroma3.siw.progettopersonale.service.UtenteService;
import it.uniroma3.siw.progettopersonale.model.Utente;

import java.util.List;

/**
 * Rende disponibili a tutti i template alcuni dati comuni, secondo il pattern
 * mostrato a lezione (slide "Autenticazione e autorizzazione", "Ottenere info
 * sull'utente loggato"): un @ControllerAdvice con metodi @ModelAttribute, cosi'
 * ogni template puo' leggerli senza che ogni singolo controller debba passarli
 * esplicitamente nel Model.
 */
@ControllerAdvice
public class GlobalController {

    private final AnimaleService animaleService;
    private final UtenteService utenteService;

    public GlobalController(AnimaleService animaleService, UtenteService utenteService) {
        this.animaleService = animaleService;
        this.utenteService = utenteService;
    }

    /** L'utente autenticato (null se non autenticato), letto dal Principal nel SecurityContextHolder. */
    @ModelAttribute("userDetails")
    public UserDetails getUser() {
        UserDetails user = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            user = (UserDetails) authentication.getPrincipal();
        }
        return user;
    }

    /** Le specie di animali disponibili, per il menu di navigazione e la home. */
    @ModelAttribute("specieDisponibili")
    public List<String> getSpecieDisponibili() {
        return animaleService.findSpecieDisponibili();
    }

    /**
     * I dati anagrafici (Utente) dell'utente autenticato, null se anonimo: utile nei
     * template per confronti sull'id (es. "e' una mia recensione?"), oltre a
     * ${userDetails.username}.
     */
    @ModelAttribute("utenteAutenticato")
    public Utente getUtenteAutenticato() {
        UserDetails userDetails = getUser();
        if (userDetails == null) {
            return null;
        }
        return utenteService.findByUsername(userDetails.getUsername());
    }
}
