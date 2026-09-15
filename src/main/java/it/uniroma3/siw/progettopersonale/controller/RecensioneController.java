package it.uniroma3.siw.progettopersonale.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import it.uniroma3.siw.progettopersonale.exception.AccessoNonAutorizzatoException;
import it.uniroma3.siw.progettopersonale.exception.RecensioneGiaPresenteException;
import it.uniroma3.siw.progettopersonale.model.Recensione;
import it.uniroma3.siw.progettopersonale.model.Ruolo;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.service.RecensioneService;
import it.uniroma3.siw.progettopersonale.service.UtenteService;

/**
 * Recensioni sull'operato degli amministratori del rifugio.
 *
 * Chi puo' fare cosa:
 *   - chiunque (anche non autenticato) puo' LEGGERE l'elenco su /recensioni
 *   - solo un utente registrato con ruolo UTENTE puo' scriverne, modificare
 *     ed eliminare le PROPRIE
 *   - l'ADMIN le vede ma non puo' scriverne (lo impone anche SecurityConfig,
 *     che protegge le rotte di scrittura con hasAuthority("UTENTE"))
 */
@Controller
public class RecensioneController {

    private final RecensioneService recensioneService;
    private final UtenteService utenteService;

    public RecensioneController(RecensioneService recensioneService,
                                 UtenteService utenteService) {
        this.recensioneService = recensioneService;
        this.utenteService = utenteService;
    }

    /** L'Utente autenticato, oppure null se la pagina la sta guardando un anonimo. */
    private Utente utenteAutenticatoOppureNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return utenteService.findByUsername(userDetails.getUsername());
    }

    /** L'Utente autenticato; usato nelle rotte che richiedono per forza il login. */
    private Utente utenteAutenticato() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return utenteService.findByUsername(userDetails.getUsername());
    }

    /** Elenco pubblico delle recensioni. */
    @GetMapping("/recensioni")
    public String elenco(Model model) {
        model.addAttribute("recensioni", recensioneService.findTutte());

        Utente utente = utenteAutenticatoOppureNull();
        // Solo un utente normale registrato puo' scrivere: l'admin no.
        model.addAttribute("puoRecensire", utente != null && !utente.isAdmin());
        return "recensioni";
    }

    /** Form per scrivere una nuova recensione: si sceglie l'amministratore da recensire. */
    @GetMapping("/recensioni/nuova")
    public String formNuova(Model model) {
        model.addAttribute("recensione", new Recensione());
        model.addAttribute("amministratori", utenteService.findByRuolo(Ruolo.ADMIN));
        return "recensioneForm";
    }

    @PostMapping("/recensioni")
    public String creaRecensione(@RequestParam("adminId") Long adminId,
                                  @Valid @ModelAttribute("recensione") Recensione recensioneForm,
                                  BindingResult bindingResult,
                                  Model model) {

        List<Utente> amministratori = utenteService.findByRuolo(Ruolo.ADMIN);

        if (bindingResult.hasErrors()) {
            model.addAttribute("amministratori", amministratori);
            return "recensioneForm";
        }

        try {
            recensioneService.creaRecensione(adminId, utenteAutenticato().getId(), recensioneForm);
            return "redirect:/recensioni";
        } catch (RecensioneGiaPresenteException | AccessoNonAutorizzatoException e) {
            model.addAttribute("amministratori", amministratori);
            bindingResult.reject("erroreRecensione", e.getMessage());
            return "recensioneForm";
        }
    }

    /** Form per modificare la propria recensione (l'amministratore recensito non cambia). */
    @GetMapping("/recensioni/{id}/modifica")
    public String formModifica(@PathVariable("id") Long id, Model model) {
        Recensione recensione = recensioneService.findById(id);
        if (!recensione.getAutore().getId().equals(utenteAutenticato().getId())) {
            throw new AccessoNonAutorizzatoException("Non puoi modificare la recensione di un altro utente.");
        }
        model.addAttribute("recensione", recensione);
        model.addAttribute("amministratori", utenteService.findByRuolo(Ruolo.ADMIN));
        return "recensioneForm";
    }

    @PostMapping("/recensioni/{id}/modifica")
    public String modificaRecensione(@PathVariable("id") Long id,
                                      @Valid @ModelAttribute("recensione") Recensione recensioneForm,
                                      BindingResult bindingResult,
                                      Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("amministratori", utenteService.findByRuolo(Ruolo.ADMIN));
            return "recensioneForm";
        }

        recensioneService.modificaRecensione(id, utenteAutenticato().getId(), recensioneForm);
        return "redirect:/recensioni";
    }

    @PostMapping("/recensioni/{id}/elimina")
    public String eliminaRecensione(@PathVariable("id") Long id) {
        recensioneService.eliminaRecensione(id, utenteAutenticato().getId());
        return "redirect:/recensioni";
    }
}
