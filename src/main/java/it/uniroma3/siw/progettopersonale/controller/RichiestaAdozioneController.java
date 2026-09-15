package it.uniroma3.siw.progettopersonale.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

/**
 * Richieste di adozione lato utente: invio di una nuova richiesta (scegliendo
 * l'animale e uno degli slot orari resi disponibili dall'admin), modifica e
 * annullamento delle proprie richieste ancora in attesa.
 */
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

    /** L'Utente attualmente autenticato. */
    private Utente utenteAutenticato() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return utenteService.findByUsername(userDetails.getUsername());
    }

    /** Rimette nel Model quello che serve a ridisegnare la form di una nuova richiesta. */
    private void popolaFormNuova(Long animaleId, Model model, String errore) {
        model.addAttribute("animale", animaleService.findById(animaleId));
        model.addAttribute("turniDisponibili", turnoService.findPrenotabili());
        model.addAttribute("errore", errore);
    }

    /**
     * Rimette nel Model quello che serve alla form di modifica: gli slot ancora
     * liberi PIU' quello gia' prenotato da questa richiesta, che altrimenti non
     * comparirebbe fra le scelte (non e' libero: e' occupato proprio da lei).
     */
    private void popolaFormModifica(RichiestaAdozione richiesta, Model model, String errore) {
        var turni = new java.util.ArrayList<>(turnoService.findPrenotabili());
        if (richiesta.getTurno() != null) {
            turni.add(0, richiesta.getTurno());
        }
        model.addAttribute("richiesta", richiesta);
        model.addAttribute("turniDisponibili", turni);
        model.addAttribute("errore", errore);
    }

    // ==================== nuova richiesta ====================

    /**
     * Form per una nuova richiesta. Se l'utente ha gia' una richiesta in attesa
     * per questo animale non puo' inviarne una seconda: viene mandato alle sue
     * richieste, dove trova quella esistente e puo' modificarla o annullarla.
     */
    @GetMapping("/animali/{id}/richiedi-adozione")
    public String formRichiesta(@PathVariable("id") Long id, Model model) {
        if (richiestaAdozioneService.haRichiestaInAttesa(id, utenteAutenticato().getId())) {
            return "redirect:/le-mie-richieste";
        }
        popolaFormNuova(id, model, null);
        return "richiediAdozione";
    }

    @PostMapping("/animali/{id}/richiedi-adozione")
    public String inviaRichiesta(@PathVariable("id") Long id,
                                  @RequestParam(value = "motivazione", required = false) String motivazione,
                                  @RequestParam(value = "turnoId", required = false) Long turnoId,
                                  Model model) {
        try {
            richiestaAdozioneService.creaRichiesta(id, utenteAutenticato().getId(), motivazione, turnoId);
            return "redirect:/le-mie-richieste";
        } catch (RichiestaGiaPresenteException e) {
            // Doppione: la richiesta esiste gia' e la trova fra le sue.
            return "redirect:/le-mie-richieste";
        } catch (AnimaleNonDisponibileException | AccessoNonAutorizzatoException
                 | OperazioneNonConsentitaException e) {
            popolaFormNuova(id, model, e.getMessage());
            return "richiediAdozione";
        }
    }

    // ==================== le mie richieste ====================

    @GetMapping("/le-mie-richieste")
    public String leMieRichieste(@RequestParam(value = "cerca", required = false) String cerca,
                                  Model model) {
        Utente utente = utenteAutenticato();
        model.addAttribute("richieste",
                richiestaAdozioneService.findByAdottanteIdAndSearch(utente.getId(), cerca));
        model.addAttribute("cerca", cerca != null ? cerca : "");
        return "leMieRichieste";
    }

    /** Form per modificare una propria richiesta ancora in attesa. */
    @GetMapping("/le-mie-richieste/{id}/modifica")
    public String formModifica(@PathVariable("id") Long id, Model model) {
        RichiestaAdozione richiesta = richiestaAdozioneService.findById(id);

        if (!richiesta.getAdottante().getId().equals(utenteAutenticato().getId())) {
            throw new AccessoNonAutorizzatoException("Non puoi modificare una richiesta di un altro utente.");
        }
        if (richiesta.getStato() != StatoRichiesta.IN_ATTESA) {
            throw new OperazioneNonConsentitaException(
                    "La richiesta è già stata gestita e non può più essere modificata.");
        }

        popolaFormModifica(richiesta, model, null);
        return "modificaRichiesta";
    }

    @PostMapping("/le-mie-richieste/{id}/modifica")
    public String salvaModifica(@PathVariable("id") Long id,
                                 @RequestParam(value = "motivazione", required = false) String motivazione,
                                 @RequestParam(value = "turnoId", required = false) Long turnoId,
                                 Model model) {
        try {
            richiestaAdozioneService.modificaRichiesta(id, utenteAutenticato().getId(), motivazione, turnoId);
            return "redirect:/le-mie-richieste";
        } catch (OperazioneNonConsentitaException e) {
            popolaFormModifica(richiestaAdozioneService.findById(id), model, e.getMessage());
            return "modificaRichiesta";
        }
    }

    @PostMapping("/le-mie-richieste/{id}/elimina")
    public String eliminaRichiesta(@PathVariable("id") Long id) {
        richiestaAdozioneService.eliminaRichiesta(id, utenteAutenticato().getId());
        return "redirect:/le-mie-richieste";
    }
}
