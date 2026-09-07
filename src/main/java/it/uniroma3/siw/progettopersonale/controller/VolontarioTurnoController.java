package it.uniroma3.siw.progettopersonale.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import it.uniroma3.siw.progettopersonale.model.Turno;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.service.TurnoService;
import it.uniroma3.siw.progettopersonale.service.UtenteService;

/**
 * Controller che gestisce il CRUD dei turni per i volontari autenticati.
 * Tutte le rotte /volontario/** sono protette da SecurityConfig con hasAuthority("VOLONTARIO").
 * Ogni turno rappresenta una disponibilita' dichiarata dal volontario autenticato: non si
 * sceglie ne' un altro volontario ne' un animale, e ogni volontario vede solo i propri turni.
 */
@Controller
public class VolontarioTurnoController {

    private final TurnoService turnoService;
    private final UtenteService utenteService;

    public VolontarioTurnoController(TurnoService turnoService,
                                      UtenteService utenteService) {
        this.turnoService = turnoService;
        this.utenteService = utenteService;
    }

    /** Elenco dei soli turni del volontario autenticato, con filtro opzionale per data. */
    @GetMapping("/volontario/turni")
    public String elenco(@RequestParam(value = "data", required = false) String dataStr,
                          Principal principal,
                          Model model) {
        Utente volontario = utenteService.findByUsername(principal.getName());
        List<Turno> turnoList;
        LocalDate dataFiltro = null;

        if (dataStr != null && !dataStr.isBlank()) {
            dataFiltro = LocalDate.parse(dataStr);
            turnoList = turnoService.findByVolontarioAndData(volontario, dataFiltro);
        } else {
            turnoList = turnoService.findByVolontario(volontario);
        }

        model.addAttribute("turnoList", turnoList);
        model.addAttribute("dataFiltro", dataFiltro);
        return "volontario/turni";
    }

    /** Form per dichiarare un nuovo turno di disponibilita'. */
    @GetMapping("/volontario/turni/nuovo")
    public String formNuovo(Model model) {
        model.addAttribute("turno", null);
        model.addAttribute("errore", null);
        return "volontario/turnoForm";
    }

    /** Form per modificare un proprio turno esistente. */
    @GetMapping("/volontario/turni/{id}/modifica")
    public String formModifica(@PathVariable("id") Long id, Principal principal, Model model) {
        Turno turno = turnoService.findById(id);
        if (turno == null) {
            return "redirect:/volontario/turni";
        }
        Utente volontario = utenteService.findByUsername(principal.getName());
        if (turno.getVolontario() == null || !turno.getVolontario().getId().equals(volontario.getId())) {
            return "redirect:/volontario/turni";
        }
        model.addAttribute("turno", turno);
        model.addAttribute("errore", null);
        return "volontario/turnoForm";
    }

    /** Salva un turno nuovo o modificato, a seconda che l'id sia presente. */
    @PostMapping("/volontario/turni")
    public String salva(@RequestParam(value = "id", required = false) Long id,
                        @RequestParam("data") String dataStr,
                        @RequestParam("oraInizio") String oraInizioStr,
                        @RequestParam("oraFine") String oraFineStr,
                        @RequestParam(value = "note", required = false) String note,
                        Principal principal,
                        Model model) {

        Utente volontario = utenteService.findByUsername(principal.getName());
        try {
            LocalDate data = LocalDate.parse(dataStr);
            LocalTime oraInizio = LocalTime.parse(oraInizioStr);
            LocalTime oraFine = LocalTime.parse(oraFineStr);
            if (id != null) {
                turnoService.modificaTurno(id, volontario.getId(), data, oraInizio, oraFine, note);
            } else {
                turnoService.creaTurno(volontario.getId(), data, oraInizio, oraFine, note);
            }
            return "redirect:/volontario/turni";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("turno", null);
            model.addAttribute("errore", e.getMessage());
            return "volontario/turnoForm";
        }
    }

    /** Elimina un proprio turno per id. */
    @PostMapping("/volontario/turni/{id}/elimina")
    public String elimina(@PathVariable("id") Long id, Principal principal) {
        Utente volontario = utenteService.findByUsername(principal.getName());
        turnoService.deleteById(id, volontario.getId());
        return "redirect:/volontario/turni";
    }
}
