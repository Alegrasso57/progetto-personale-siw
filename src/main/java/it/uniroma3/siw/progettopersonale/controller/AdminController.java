package it.uniroma3.siw.progettopersonale.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.service.VolontarioService;

/**
 * Controller per la sezione di utilità admin (accessibile ai VOLONTARIO).
 * Dimostra il problema N+1 e la sua soluzione via JOIN FETCH, sulla relazione
 * tra i volontari e i turni che mettono a disposizione.
 */
@Controller
public class AdminController {

    private final VolontarioService volontarioService;

    public AdminController(VolontarioService volontarioService) {
        this.volontarioService = volontarioService;
    }

    /**
     * Pagina con resoconto e spiegazione della query N+1 sulla relazione
     * Volontario -> Turni. Esegue la query con JOIN FETCH e fornisce i dati
     * al template.
     */
    @GetMapping("/admin/analisi-prestazioni")
    public String analisiPrestazioni(Model model) {
        // Query con JOIN FETCH: una sola query per caricare volontari + turni
        List<Utente> volontariConTurni = volontarioService.findTuttiConTurni();

        // Numero totale di turni tra tutti i volontari
        int totaleTurni = volontariConTurni.stream()
                .mapToInt(v -> v.getTurni().size())
                .sum();

        // Numero di volontari
        int totaleVolontari = volontariConTurni.size();

        model.addAttribute("volontariConTurni", volontariConTurni);
        model.addAttribute("totaleVolontari", totaleVolontari);
        model.addAttribute("totaleTurni", totaleTurni);

        // Numero di query con N+1 (1 per la lista dei volontari + 1 per ogni volontario)
        model.addAttribute("queriesSenzaSoluzione", 1 + totaleVolontari);
        // Numero di query con JOIN FETCH (sempre 1)
        model.addAttribute("queriesConSoluzione", 1);

        return "admin/analisiPrestazioni";
    }
}
