package it.uniroma3.siw.progettopersonale.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import it.uniroma3.siw.progettopersonale.exception.AccessoNonAutorizzatoException;
import it.uniroma3.siw.progettopersonale.exception.AnimaleNonTrovatoException;
import it.uniroma3.siw.progettopersonale.exception.OperazioneNonConsentitaException;
import it.uniroma3.siw.progettopersonale.exception.RecensioneNonTrovataException;
import it.uniroma3.siw.progettopersonale.exception.RichiestaAdozioneNonTrovataException;
import it.uniroma3.siw.progettopersonale.exception.TurnoNonTrovatoException;
import it.uniroma3.siw.progettopersonale.exception.UtenteNonTrovatoException;

/**
 * Gestore centralizzato degli errori (slide "Controller, View, Gestione Errori"):
 * i service lanciano un'eccezione di dominio, i controller (quando non devono
 * ripresentare una form) non fanno nulla di speciale, e questa classe intercetta
 * l'eccezione scegliendo la pagina di errore opportuna.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({AnimaleNonTrovatoException.class, UtenteNonTrovatoException.class,
            RecensioneNonTrovataException.class, RichiestaAdozioneNonTrovataException.class,
            TurnoNonTrovatoException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNonTrovato(RuntimeException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "error/404";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFound(NoResourceFoundException e, Model model) {
        model.addAttribute("errorMessage", "Pagina non trovata.");
        return "error/404";
    }

    @ExceptionHandler(AccessoNonAutorizzatoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessoNonAutorizzato(AccessoNonAutorizzatoException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "error/accesso-negato";
    }

    @ExceptionHandler(OperazioneNonConsentitaException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleOperazioneNonConsentita(OperazioneNonConsentitaException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "error/operazione-non-consentita";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnexpectedException(Exception e, Model model) {
        logger.error("Errore interno inatteso", e);
        model.addAttribute("errorMessage", "Si è verificato un errore interno. Riprovare più tardi.");
        return "error/500";
    }
}
