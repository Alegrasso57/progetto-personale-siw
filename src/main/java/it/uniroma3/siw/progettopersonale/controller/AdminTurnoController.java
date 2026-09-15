package it.uniroma3.siw.progettopersonale.controller;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.progettopersonale.exception.OperazioneNonConsentitaException;
import it.uniroma3.siw.progettopersonale.model.Turno;
import it.uniroma3.siw.progettopersonale.service.TurnoService;

/**
 * Gestione degli slot di disponibilita' per le visite, riservata all'ADMIN
 * (la rotta /admin/** e' protetta da SecurityConfig con hasAuthority("ADMIN")).
 *
 * L'admin dichiara data e fascia oraria in cui e' disponibile; gli utenti
 * normali scelgono poi uno di questi slot quando inviano una richiesta di
 * adozione (vedi RichiestaAdozioneController).
 */
@Controller
public class AdminTurnoController {

    private final TurnoService turnoService;

    public AdminTurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    /** Elenco di tutti gli slot inseriti, liberi e prenotati. */
    @GetMapping("/admin/turni")
    public String elenco(Model model) {
        model.addAttribute("turnoList", turnoService.findTutti());
        return "admin/turni";
    }

    /** Form per inserire un nuovo slot. */
    @GetMapping("/admin/turni/nuovo")
    public String formNuovo(Model model) {
        model.addAttribute("turno", null);
        model.addAttribute("errore", null);
        return "admin/turnoForm";
    }

    /** Form per modificare uno slot esistente: stessa pagina, precompilata. */
    @GetMapping("/admin/turni/{id}/modifica")
    public String formModifica(@PathVariable("id") Long id, Model model) {
        model.addAttribute("turno", turnoService.findById(id));
        model.addAttribute("errore", null);
        return "admin/turnoForm";
    }

    /**
     * Salva un nuovo slot. I controlli (ora di fine dopo quella di inizio,
     * nessuna sovrapposizione con un turno esistente) stanno in TurnoService:
     * qui si cattura solo l'eccezione per ripresentare la form con il messaggio.
     */
    @PostMapping("/admin/turni")
    public String salva(@RequestParam("data") String dataStr,
                         @RequestParam("oraInizio") String oraInizioStr,
                         @RequestParam("oraFine") String oraFineStr,
                         Model model) {
        try {
            turnoService.creaTurno(LocalDate.parse(dataStr),
                    LocalTime.parse(oraInizioStr), LocalTime.parse(oraFineStr));
            return "redirect:/admin/turni";

        } catch (OperazioneNonConsentitaException e) {
            model.addAttribute("turno", null);
            model.addAttribute("errore", e.getMessage());
            return "admin/turnoForm";
        } catch (java.time.format.DateTimeParseException e) {
            model.addAttribute("turno", null);
            model.addAttribute("errore", "Data oppure orario non validi.");
            return "admin/turnoForm";
        }
    }

    /** Salva le modifiche a uno slot esistente. */
    @PostMapping("/admin/turni/{id}/modifica")
    public String salvaModifica(@PathVariable("id") Long id,
                                 @RequestParam("data") String dataStr,
                                 @RequestParam("oraInizio") String oraInizioStr,
                                 @RequestParam("oraFine") String oraFineStr,
                                 Model model) {
        try {
            turnoService.modificaTurno(id, LocalDate.parse(dataStr),
                    LocalTime.parse(oraInizioStr), LocalTime.parse(oraFineStr));
            return "redirect:/admin/turni";

        } catch (OperazioneNonConsentitaException e) {
            model.addAttribute("turno", turnoService.findById(id));
            model.addAttribute("errore", e.getMessage());
            return "admin/turnoForm";
        } catch (java.time.format.DateTimeParseException e) {
            model.addAttribute("turno", turnoService.findById(id));
            model.addAttribute("errore", "Data oppure orario non validi.");
            return "admin/turnoForm";
        }
    }

    /** Elimina uno slot, se non e' gia' stato prenotato. */
    @PostMapping("/admin/turni/{id}/elimina")
    public String elimina(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            turnoService.eliminaTurno(id);
        } catch (OperazioneNonConsentitaException e) {
            redirectAttributes.addFlashAttribute("erroreTurno", e.getMessage());
        }
        return "redirect:/admin/turni";
    }
}
