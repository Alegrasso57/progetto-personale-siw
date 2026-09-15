package it.uniroma3.siw.progettopersonale.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.service.UtenteService;

/**
 * Rende disponibili a TUTTI i template i dati sull'utente autenticato, secondo
 * il pattern visto a lezione (slide "Autenticazione e autorizzazione",
 * "Ottenere info sull'utente loggato"): un @ControllerAdvice con metodi
 * @ModelAttribute, cosi' ogni template li legge senza che ogni controller
 * debba passarli esplicitamente nel Model.
 *
 * E' anche il punto giusto dove mettere eventuali dati comuni a tutte le
 * pagine (per esempio dei count da mostrare nella nav bar): basta aggiungere
 * un metodo annotato con @ModelAttribute("nome") e il template lo legge
 * con ${nome}.
 */
@ControllerAdvice
public class GlobalController {

    private final UtenteService utenteService;

    public GlobalController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    /** L'utente autenticato secondo Spring Security (null se anonimo). */
    @ModelAttribute("userDetails")
    public UserDetails getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return (UserDetails) authentication.getPrincipal();
    }

    
    @ModelAttribute("utenteAutenticato")
    public Utente getUtenteAutenticato() {
        UserDetails userDetails = getUser();
        if (userDetails == null) {
            return null;
        }
        return utenteService.findByUsername(userDetails.getUsername());
    }
}
