package it.uniroma3.siw.progettopersonale.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import it.uniroma3.siw.progettopersonale.service.AnimaleService;
import it.uniroma3.siw.progettopersonale.service.RecensioneVolontarioService;
import it.uniroma3.siw.progettopersonale.service.SpecieService;
import it.uniroma3.siw.progettopersonale.service.TurnoService;
import it.uniroma3.siw.progettopersonale.service.UtenteService;
import it.uniroma3.siw.progettopersonale.service.VolontarioService;
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
    private final TurnoService turnoService;
    private final RecensioneVolontarioService recensioneVolontarioService;
    private final SpecieService specieService;
    private final VolontarioService volontarioService;

    public GlobalController(AnimaleService animaleService, UtenteService utenteService,
                             TurnoService turnoService, RecensioneVolontarioService recensioneVolontarioService,
                             SpecieService specieService, VolontarioService volontarioService) {
        this.animaleService = animaleService;
        this.utenteService = utenteService;
        this.turnoService = turnoService;
        this.recensioneVolontarioService = recensioneVolontarioService;
        this.specieService = specieService;
        this.volontarioService = volontarioService;
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
        return specieService.findDisponibili();
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

    
    @ModelAttribute("numeroAnimali")
    public long getNumeroAnimali() {
        return animaleService.count();
    }

    @ModelAttribute("numeroVolontari")
    public long getNumeroVolontari() {
        return volontarioService.count();
    }

    @ModelAttribute("numeroTurni")
    public long getNumeroTurni() {
        return turnoService.count();
    }

    @ModelAttribute("numeroRecensioniVolontari")
    public long getNumeroRecensioniVolontari() {
        return recensioneVolontarioService.count();
    }

    @ModelAttribute("numeroSpecie")
    public int getNumeroSpecie() {
        return getSpecieDisponibili().size();
    }
}
