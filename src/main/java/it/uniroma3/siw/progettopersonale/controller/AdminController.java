package it.uniroma3.siw.progettopersonale.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.service.AnimaleService;

/**
 * Sezione di utilita' riservata all'ADMIN.
 *
 * Mostra il problema N+1 sulla relazione UNO-A-MOLTI / MOLTI-A-UNO fra
 * Animale e RichiestaAdozione, e come risolverlo con JOIN FETCH.
 */
@Controller
public class AdminController {

    private final AnimaleService animaleService;

    public AdminController(AnimaleService animaleService) {
        this.animaleService = animaleService;
    }

    @GetMapping("/admin/analisi-prestazioni")
    public String analisiPrestazioni(Model model) {

        // Query con JOIN FETCH: una sola query carica animali + richieste.
        List<Animale> animaliConRichieste = animaleService.findTuttiConRichieste();

        int totaleAnimali = animaliConRichieste.size();
        int totaleRichieste = animaliConRichieste.stream()
                .mapToInt(a -> a.getRichiesteAdozione().size())
                .sum();

        model.addAttribute("animaliConRichieste", animaliConRichieste);
        model.addAttribute("totaleAnimali", totaleAnimali);
        model.addAttribute("totaleRichieste", totaleRichieste);

        // Senza JOIN FETCH: 1 query per la lista + 1 per le richieste di ogni animale.
        model.addAttribute("queriesSenzaSoluzione", 1 + totaleAnimali);
        // Con JOIN FETCH: sempre e solo 1.
        model.addAttribute("queriesConSoluzione", 1);

        return "admin/analisiPrestazioni";
    }
}
