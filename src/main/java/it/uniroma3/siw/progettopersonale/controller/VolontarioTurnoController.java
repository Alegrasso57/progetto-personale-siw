package it.uniroma3.siw.progettopersonale.controller;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
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
import it.uniroma3.siw.progettopersonale.exception.OperazioneNonConsentitaException;
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
                          Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Utente volontario = utenteService.findByUsername(userDetails.getUsername());
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
        model.addAttribute("turno", new Turno());
        return "volontario/turnoForm";
    }

    /** Form per modificare un proprio turno esistente. */
    @GetMapping("/volontario/turni/{id}/modifica")
    public String formModifica(@PathVariable("id") Long id, Model model) {
        Turno turno = turnoService.findById(id);
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Utente volontario = utenteService.findByUsername(userDetails.getUsername());
        if (turno.getVolontario() == null || !turno.getVolontario().getId().equals(volontario.getId())) {
            throw new AccessoNonAutorizzatoException("Non sei autorizzato a modificare questo turno.");
        }
        model.addAttribute("turno", turno);
        return "volontario/turnoForm";
    }

    /** Salva un turno nuovo o modificato, a seconda che l'id sia presente. Data/orari sono
     *  gia' stati validati come non-null da @Valid sull'entita' Turno; qui il service verifica
     *  solo le regole di business (sovrapposizione con altri turni, ora fine dopo ora inizio). */
    @PostMapping("/volontario/turni")
    public String salva(@Valid @ModelAttribute("turno") Turno turnoForm,
                         BindingResult bindingResult,
                         Model model) {

        if (bindingResult.hasErrors()) {
            return "volontario/turnoForm";
        }

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Utente volontario = utenteService.findByUsername(userDetails.getUsername());
        try {
            if (turnoForm.getId() != null) {
                turnoService.modificaTurno(turnoForm.getId(), volontario.getId(), turnoForm.getData(),
                        turnoForm.getOraInizio(), turnoForm.getOraFine(), turnoForm.getNote());
            } else {
                turnoService.creaTurno(volontario.getId(), turnoForm.getData(),
                        turnoForm.getOraInizio(), turnoForm.getOraFine(), turnoForm.getNote());
            }
            return "redirect:/volontario/turni";
        } catch (OperazioneNonConsentitaException e) {
            model.addAttribute("errore", e.getMessage());
            return "volontario/turnoForm";
        }
    }

    /** Elimina un proprio turno per id. */
    @PostMapping("/volontario/turni/{id}/elimina")
    public String elimina(@PathVariable("id") Long id) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Utente volontario = utenteService.findByUsername(userDetails.getUsername());
        turnoService.deleteById(id, volontario.getId());
        return "redirect:/volontario/turni";
    }
}
