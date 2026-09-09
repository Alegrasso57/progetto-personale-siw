package it.uniroma3.siw.progettopersonale.controller;

import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import it.uniroma3.siw.progettopersonale.exception.AccessoNonAutorizzatoException;
import it.uniroma3.siw.progettopersonale.exception.AnimaleNonDisponibileException;
import it.uniroma3.siw.progettopersonale.exception.OperazioneNonConsentitaException;
import it.uniroma3.siw.progettopersonale.exception.RichiestaGiaPresenteException;
import it.uniroma3.siw.progettopersonale.model.RichiestaAdozione;
import it.uniroma3.siw.progettopersonale.model.StatoRichiesta;
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
     *  dichiarato disponibili e non sono ancora stati prenotati). */
    private void popolaModelloPrenotazione(Model model) {
        model.addAttribute("turniDisponibili", turnoService.findDisponibiliPerPrenotazione());
    }

    @GetMapping("/animali/{id}/richiedi-adozione")
    public String formRichiesta(@PathVariable("id") Long id, Model model) {
        model.addAttribute("animale", animaleService.findById(id));
        model.addAttribute("errore", null);
        popolaModelloPrenotazione(model);
        return "richiediAdozione";
    }

    @PostMapping("/animali/{id}/richiedi-adozione")
    public String inviaRichiesta(@PathVariable("id") Long id,
                                  @RequestParam("motivazione") String motivazione,
                                  @RequestParam(value = "turnoId", required = false) List<Long> turnoIdsSelezionati,
                                  Principal principal,
                                  Model model) {
        Utente adottante = utenteService.findByUsername(principal.getName());
        try {
            richiestaAdozioneService.creaRichiesta(id, adottante.getId(), motivazione, turnoIdsSelezionati);
            return "redirect:/le-mie-richieste";
        } catch (AnimaleNonDisponibileException | AccessoNonAutorizzatoException
                 | RichiestaGiaPresenteException | OperazioneNonConsentitaException e) {
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
        Utente adottante = utenteService.findByUsername(principal.getName());
        richiestaAdozioneService.eliminaRichiesta(id, adottante.getId());
        return "redirect:/le-mie-richieste";
    }

    @GetMapping("/le-mie-richieste/{id}/modifica")
    public String formModificaTurni(@PathVariable("id") Long id, Principal principal, Model model) {
        RichiestaAdozione richiesta = richiestaAdozioneService.findById(id);
        Utente adottante = utenteService.findByUsername(principal.getName());
        if (!richiesta.getAdottante().getId().equals(adottante.getId())) {
            throw new AccessoNonAutorizzatoException("Non puoi modificare una richiesta di un altro utente.");
        }
        if (richiesta.getStato() != StatoRichiesta.IN_ATTESA) {
            throw new OperazioneNonConsentitaException("La richiesta è già stata gestita e non può più essere modificata.");
        }
        model.addAttribute("richiesta", richiesta);
        model.addAttribute("turniDisponibili", turnoService.findDisponibiliPerModifica(richiesta));
        model.addAttribute("errore", null);
        return "modificaTurniRichiesta";
    }

    @PostMapping("/le-mie-richieste/{id}/modifica")
    public String salvaModificaTurni(@PathVariable("id") Long id,
                                      @RequestParam(value = "turnoId", required = false) List<Long> turnoIdsSelezionati,
                                      Principal principal,
                                      Model model) {
        Utente adottante = utenteService.findByUsername(principal.getName());
        try {
            richiestaAdozioneService.modificaTurniPrenotati(id, adottante.getId(), turnoIdsSelezionati);
            return "redirect:/le-mie-richieste";
        } catch (OperazioneNonConsentitaException e) {
            RichiestaAdozione richiesta = richiestaAdozioneService.findById(id);
            model.addAttribute("richiesta", richiesta);
            model.addAttribute("turniDisponibili", turnoService.findDisponibiliPerModifica(richiesta));
            model.addAttribute("errore", e.getMessage());
            return "modificaTurniRichiesta";
        }
    }
}
