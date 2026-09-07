package it.uniroma3.siw.progettopersonale.controller;

import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.service.AnimaleService;
import it.uniroma3.siw.progettopersonale.service.RichiestaAdozioneService;
import it.uniroma3.siw.progettopersonale.service.TurnoService;
import it.uniroma3.siw.progettopersonale.service.UtenteService;

@Controller
public class RichiestaAdozioneController {

    private final RichiestaAdozioneService richiestaAdozioneService;
    private final AnimaleService animaleService;
    private final UtenteService utenteService;
    private final TurnoService turnoService;

    public RichiestaAdozioneController(RichiestaAdozioneService richiestaAdozioneService,
                                        AnimaleService animaleService,
                                        UtenteService utenteService,
                                        TurnoService turnoService) {
        this.richiestaAdozioneService = richiestaAdozioneService;
        this.animaleService = animaleService;
        this.utenteService = utenteService;
        this.turnoService = turnoService;
    }

    /** Popola gli attributi per la scelta del turno (i turni che i volontari hanno
     *  dichiarato disponibili e non sono ancora stati prenotati) e dei volontari. */
    private void popolaModelloPrenotazione(Model model) {
        model.addAttribute("turniDisponibili", turnoService.findDisponibiliPerPrenotazione());
        model.addAttribute("volontari", utenteService.findVolontari());
    }

    @GetMapping("/animali/{id}/richiedi-adozione")
    public String formRichiesta(@PathVariable("id") Long id, Model model) {
        Animale animale = animaleService.findById(id);
        if (animale == null) {
            return "redirect:/animali";
        }
        model.addAttribute("animale", animale);
        model.addAttribute("errore", null);
        popolaModelloPrenotazione(model);
        return "richiediAdozione";
    }

    @PostMapping("/animali/{id}/richiedi-adozione")
    public String inviaRichiesta(@PathVariable("id") Long id,
                                  @ModelAttribute("motivazione") String motivazione,
                                  @RequestParam(value = "volontarioPreferitoId", required = false) String volontarioPreferitoIdStr,
                                  @RequestParam(value = "turnoId", required = false) List<Long> turnoIdsSelezionati,
                                  Principal principal,
                                  Model model) {
        Utente adottante = utenteService.findByUsername(principal.getName());
        try {
            // Il <select> manda stringa vuota per "nessuna preferenza": va gestita a
            // mano, altrimenti Spring fallirebbe a convertirla direttamente in Long.
            Long volontarioPreferitoId = (volontarioPreferitoIdStr != null && !volontarioPreferitoIdStr.isBlank())
                    ? Long.parseLong(volontarioPreferitoIdStr)
                    : null;
            richiestaAdozioneService.creaRichiesta(id, adottante.getId(), motivazione, volontarioPreferitoId, turnoIdsSelezionati);
            return "redirect:/le-mie-richieste";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("animale", animaleService.findById(id));
            model.addAttribute("errore", e.getMessage());
            popolaModelloPrenotazione(model);
            return "richiediAdozione";
        }
    }

    @GetMapping("/le-mie-richieste")
    public String leMieRichieste(@RequestParam(value = "cerca", required = false) String cerca,
                                  Principal principal, Model model) {
        Utente adottante = utenteService.findByUsername(principal.getName());
        model.addAttribute("richieste", richiestaAdozioneService.findByAdottanteIdAndSearch(adottante.getId(), cerca));
        model.addAttribute("cerca", cerca != null ? cerca : "");
        return "leMieRichieste";
    }

    @PostMapping("/le-mie-richieste/{id}/elimina")
    public String eliminaRichiesta(@PathVariable("id") Long id, Principal principal) {
        richiestaAdozioneService.eliminaRichiesta(id, principal.getName());
        return "redirect:/le-mie-richieste";
    }
}
