package it.uniroma3.siw.progettopersonale.controller;

import java.util.List;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import it.uniroma3.siw.progettopersonale.dto.AnimaleRicercaDTO;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.service.AnimaleService;
import it.uniroma3.siw.progettopersonale.service.RichiestaAdozioneService;
import it.uniroma3.siw.progettopersonale.service.UtenteService;

@Controller
public class AnimaleController {

    private final AnimaleService animaleService;
    private final RichiestaAdozioneService richiestaAdozioneService;
    private final UtenteService utenteService;

    public AnimaleController(AnimaleService animaleService,
                              RichiestaAdozioneService richiestaAdozioneService,
                              UtenteService utenteService) {
        this.animaleService = animaleService;
        this.richiestaAdozioneService = richiestaAdozioneService;
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

    /** Elenco degli animali disponibili, con ricerca e filtro per specie. */
    @GetMapping("/animali")
    public String elenco(@RequestParam(value = "cerca", required = false) String cerca,
                          @RequestParam(value = "specie", required = false) String specie,
                          Model model) {

        List<Animale> animaleList;
        if (specie != null && !specie.isBlank()) {
            animaleList = animaleService.findDisponibiliBySpecie(specie);
            model.addAttribute("specie", specie);
        } else {
            animaleList = animaleService.findDisponibiliBySearch(cerca);
        }

        model.addAttribute("animaleList", animaleList);
        model.addAttribute("cerca", cerca != null ? cerca : "");
        // Stessa lista, ma solo i campi che servono alla barra di ricerca in React
        // (vedi frontend-ricerca-animali/): la pagina la usa come risultati iniziali,
        // cosi' non deve fare una chiamata in piu' al caricamento.
        model.addAttribute("animaleListJson", animaleList.stream().map(AnimaleRicercaDTO::from).toList());
        return "animali";
    }

    /**
     * Stessa ricerca di /animali (solo per nome/specie, sugli animali
     * disponibili), in JSON: usata dalla barra di ricerca in React per
     * aggiornare i risultati mentre si scrive, senza ricaricare la pagina.
     */
    @GetMapping("/api/animali/ricerca")
    @ResponseBody
    public List<AnimaleRicercaDTO> ricercaJson(@RequestParam(value = "cerca", required = false) String cerca) {
        List<Animale> risultati = animaleService.findDisponibiliBySearch(cerca);
        return risultati.stream().map(AnimaleRicercaDTO::from).toList();
    }

    /**
     * Dettaglio di un animale.
     *
     * giaRichiesto dice se chi sta guardando ha gia' una richiesta in attesa per
     * questo animale: in quel caso la pagina non ripropone il pulsante
     * "Richiedi l'adozione" ma rimanda a "Le mie richieste".
     */
    @GetMapping("/animali/{id}")
    public String dettaglio(@PathVariable("id") Long id, Model model) {
        model.addAttribute("animale", animaleService.findById(id));

        Utente utente = utenteAutenticatoOppureNull();
        boolean giaRichiesto = utente != null
                && richiestaAdozioneService.haRichiestaInAttesa(id, utente.getId());
        model.addAttribute("giaRichiesto", giaRichiesto);

        return "animaleDetail";
    }
}
