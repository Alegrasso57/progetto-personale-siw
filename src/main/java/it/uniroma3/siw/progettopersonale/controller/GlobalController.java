package it.uniroma3.siw.progettopersonale.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Rende disponibile l'utente autenticato a tutti i template, secondo il pattern
 * mostrato a lezione (slide "Autenticazione e autorizzazione", "Ottenere info
 * sull'utente loggato"): un @ControllerAdvice con un metodo @ModelAttribute che
 * legge il Principal dal SecurityContextHolder. Cosi' ogni template puo' leggere
 * ${userDetails} (null se l'utente non e' autenticato) senza che ogni singolo
 * controller debba passarlo esplicitamente nel Model.
 */
@ControllerAdvice
public class GlobalController {

    @ModelAttribute("userDetails")
    public UserDetails getUser() {
        UserDetails user = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            user = (UserDetails) authentication.getPrincipal();
        }
        return user;
    }
}
