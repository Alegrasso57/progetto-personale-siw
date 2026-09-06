package it.uniroma3.siw.progettopersonale.controller;

import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import it.uniroma3.siw.progettopersonale.service.TurnoService;

@Controller
public class TurnoController {

    private final TurnoService turnoService;

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @GetMapping("/turni")
    public String programma(@RequestParam(value = "data", required = false) String data, Model model) {
        LocalDate filtro = (data != null && !data.isBlank()) ? LocalDate.parse(data) : LocalDate.now();
        model.addAttribute("turnoList", turnoService.findByData(filtro));
        model.addAttribute("dataSelezionata", filtro);
        return "turni";
    }
}