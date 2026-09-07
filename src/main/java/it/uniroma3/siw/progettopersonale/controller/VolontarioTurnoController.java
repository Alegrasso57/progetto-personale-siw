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
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.Turno;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.service.AnimaleService;
import it.uniroma3.siw.progettopersonale.service.TurnoService;
import it.uniroma3.siw.progettopersonale.service.UtenteService;

/**
 * Controller che gestisce il CRUD dei turni per i volontari autenticati.
 * Tutte le rotte /volontario/** sono protette da SecurityConfig con hasAuthority("VOLONTARIO").
 */
@Controller
public class VolontarioTurnoController {

    private final TurnoService turnoService;
    private final UtenteService utenteService;
    private final AnimaleService animaleService;

    public VolontarioTurnoController(TurnoService turnoService,
                                      UtenteService utenteService,
                                      AnimaleService animaleService) {
        this.turnoService = turnoService;
        this.utenteService = utenteService;
        this.animaleService = animaleService;
    }

    /** Elenco di tutti i turni, con filtro opzionale per data. */
    @GetMapping("/volontario/turni")
    public String elenco(@RequestParam(value = "data", required = false) String dataStr,
                          Model model) {
        List<Turno> turnoList;
        LocalDate dataFiltro = null;

        if (dataStr != null && !dataStr.isBlank()) {
            dataFiltro = LocalDate.parse(dataStr);
            turnoList = turnoService.findByData(dataFiltro);
        } else {
            turnoList = turnoService.findAll();
        }

        model.addAttribute("turnoList", turnoList);
        model.addAttribute("dataFiltro", dataFiltro);
        return "volontario/turni";
    }

    /** Form per creare un nuovo turno. */
    @GetMapping("/volontario/turni/nuovo")
    public String formNuovo(Model model) {
        model.addAttribute("volontarioDisponibili", utenteService.findVolontari());
        model.addAttribute("animaleDisponibili", animaleService.findAll());
        model.addAttribute("errore", null);
        return "volontario/turnoForm";
    }

    /** Salva un nuovo turno. */
    @PostMapping("/volontario/turni")
    public String creaTurno(@RequestParam("volontarioId") Long volontarioId,
                             @RequestParam("animaleId") Long animaleId,
                             @RequestParam("data") String dataStr,
                             @RequestParam("oraInizio") String oraInizioStr,
                             @RequestParam("oraFine") String oraFineStr,
                             @RequestParam(value = "note", required = false) String note,
                             Model model) {

        try {
            LocalDate data = LocalDate.parse(dataStr);
            LocalTime oraInizio = LocalTime.parse(oraInizioStr);
            LocalTime oraFine = LocalTime.parse(oraFineStr);
            turnoService.creaTurno(volontarioId, animaleId, data, oraInizio, oraFine, note);
            return "redirect:/volontario/turni";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("volontarioDisponibili", utenteService.findVolontari());
            model.addAttribute("animaleDisponibili", animaleService.findAll());
            model.addAttribute("errore", e.getMessage());
            return "volontario/turnoForm";
        }
    }

    /** Elimina un turno per id. */
    @PostMapping("/volontario/turni/{id}/elimina")
    public String elimina(@PathVariable("id") Long id) {
        turnoService.deleteById(id);
        return "redirect:/volontario/turni";
    }
}
