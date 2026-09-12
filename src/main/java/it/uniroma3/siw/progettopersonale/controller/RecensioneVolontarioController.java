package it.uniroma3.siw.progettopersonale.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import it.uniroma3.siw.progettopersonale.exception.AccessoNonAutorizzatoException;
import it.uniroma3.siw.progettopersonale.exception.RecensioneVolontarioGiaPresenteException;
import it.uniroma3.siw.progettopersonale.exception.UtenteNonTrovatoException;
import it.uniroma3.siw.progettopersonale.model.RecensioneVolontario;
import it.uniroma3.siw.progettopersonale.model.Ruolo;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.service.RecensioneVolontarioService;
import it.uniroma3.siw.progettopersonale.service.UtenteService;

/**
 * Pagina dedicata alle recensioni sull'operato dei volontari: elenco di tutte
 * le recensioni esistenti e form per inserirne una, separata dall'elenco dei
 * volontari (/volontari). Solo un utente autenticato con ruolo ADOTTANTE può
 * lasciare una recensione: un volontario non può recensire, né se stesso né
 * altri volontari.
 */
@Controller
public class RecensioneVolontarioController {

    private final RecensioneVolontarioService recensioneVolontarioService;
    private final UtenteService utenteService;

    public RecensioneVolontarioController(RecensioneVolontarioService recensioneVolontarioService,
                                           UtenteService utenteService) {
        this.recensioneVolontarioService = recensioneVolontarioService;
        this.utenteService = utenteService;
    }

    @GetMapping("/recensioni-volontari")
    public String elenco(Model model) {
        model.addAttribute("recensioniVolontari", recensioneVolontarioService.findTutte());
        model.addAttribute("volontari", utenteService.findVolontari());

        Utente utenteAutenticato = utenteAutenticatoCorrente();
        boolean puoRecensire = utenteAutenticato != null
                && utenteService.findRuolo(utenteAutenticato) != Ruolo.VOLONTARIO;
        model.addAttribute("puoRecensire", puoRecensire);

        return "recensioniVolontari";
    }

    @PostMapping("/recensioni-volontari")
    public String creaRecensione(@RequestParam("volontarioId") Long volontarioId,
                                  @RequestParam("voto") Integer voto,
                                  @RequestParam("testo") String testo,
                                  RedirectAttributes redirectAttributes) {

        Utente autore = utenteAutenticatoCorrente();
        if (autore == null || utenteService.findRuolo(autore) == Ruolo.VOLONTARIO) {
            redirectAttributes.addFlashAttribute("erroreRecensione",
                    "Solo gli utenti registrati non volontari possono lasciare una recensione.");
            return "redirect:/recensioni-volontari";
        }
        if (testo == null || testo.isBlank()) {
            redirectAttributes.addFlashAttribute("erroreRecensione", "Il testo della recensione è obbligatorio.");
            return "redirect:/recensioni-volontari";
        }
        if (voto == null || voto < 1 || voto > 5) {
            redirectAttributes.addFlashAttribute("erroreRecensione", "Il voto deve essere compreso tra 1 e 5.");
            return "redirect:/recensioni-volontari";
        }

        RecensioneVolontario recensioneForm = new RecensioneVolontario();
        recensioneForm.setVoto(voto);
        recensioneForm.setTesto(testo);

        try {
            recensioneVolontarioService.creaRecensione(volontarioId, autore.getId(), recensioneForm);
            redirectAttributes.addFlashAttribute("successoRecensione", "Recensione inserita.");
        } catch (RecensioneVolontarioGiaPresenteException | UtenteNonTrovatoException e) {
            redirectAttributes.addFlashAttribute("erroreRecensione", e.getMessage());
        }
        return "redirect:/recensioni-volontari";
    }

    @GetMapping("/recensioni-volontario/{id}/modifica")
    public String formModifica(@PathVariable("id") Long id, Model model) {
        RecensioneVolontario recensione = recensioneVolontarioService.findById(id);
        Utente autoreAutenticato = utenteAutenticatoCorrente();
        if (autoreAutenticato == null || !recensione.getAutore().getId().equals(autoreAutenticato.getId())) {
            throw new AccessoNonAutorizzatoException("Non puoi modificare la recensione di un altro utente.");
        }
        model.addAttribute("recensione", recensione);
        model.addAttribute("volontario", recensione.getVolontario());
        return "modificaRecensioneVolontario";
    }

    @PostMapping("/recensioni-volontario/{id}/modifica")
    public String modificaRecensione(@PathVariable("id") Long id,
                                      @RequestParam("voto") Integer voto,
                                      @RequestParam("testo") String testo,
                                      RedirectAttributes redirectAttributes) {

        Utente autoreAutenticato = utenteAutenticatoCorrente();
        if (autoreAutenticato == null) {
            redirectAttributes.addFlashAttribute("erroreRecensione", "Devi accedere per modificare una recensione.");
            return "redirect:/recensioni-volontari";
        }
        if (testo == null || testo.isBlank()) {
            redirectAttributes.addFlashAttribute("erroreRecensione", "Il testo della recensione è obbligatorio.");
            return "redirect:/recensioni-volontari";
        }
        if (voto == null || voto < 1 || voto > 5) {
            redirectAttributes.addFlashAttribute("erroreRecensione", "Il voto deve essere compreso tra 1 e 5.");
            return "redirect:/recensioni-volontari";
        }

        try {
            recensioneVolontarioService.modificaRecensione(id, autoreAutenticato.getId(), voto, testo);
            redirectAttributes.addFlashAttribute("successoRecensione", "Recensione modificata.");
        } catch (AccessoNonAutorizzatoException e) {
            redirectAttributes.addFlashAttribute("erroreRecensione", e.getMessage());
        }
        return "redirect:/recensioni-volontari";
    }

    @PostMapping("/recensioni-volontario/{id}/elimina")
    public String eliminaRecensione(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Utente autoreAutenticato = utenteAutenticatoCorrente();
        try {
            recensioneVolontarioService.eliminaRecensione(id, autoreAutenticato.getId());
            redirectAttributes.addFlashAttribute("successoRecensione", "Recensione eliminata.");
        } catch (AccessoNonAutorizzatoException e) {
            redirectAttributes.addFlashAttribute("erroreRecensione", e.getMessage());
        }
        return "redirect:/recensioni-volontari";
    }

    /** L'utente autenticato (dati anagrafici completi), null se anonimo. */
    private Utente utenteAutenticatoCorrente() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return utenteService.findByUsername(userDetails.getUsername());
    }
}
