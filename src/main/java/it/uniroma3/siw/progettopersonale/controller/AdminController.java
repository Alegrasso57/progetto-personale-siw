package it.uniroma3.siw.progettopersonale.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.service.AnimaleService;
import it.uniroma3.siw.progettopersonale.service.TurnoService;

/**
 * Controller per la sezione di utilità admin (accessibile ai VOLONTARIO).
 * Fornisce informazioni tecniche sul progetto, in particolare la dimostrazione
 * del problema N+1 e della sua soluzione via JOIN FETCH / @EntityGraph.
 */
@Controller
public class AdminController {

    private final AnimaleService animaleService;
    private final TurnoService turnoService;

    public AdminController(AnimaleService animaleService, TurnoService turnoService) {
        this.animaleService = animaleService;
        this.turnoService = turnoService;
    }

    /**
     * Pagina con resoconto e spiegazione della query N+1.
     * Esegue la query con JOIN FETCH e fornisce i dati al template.
     */
    @GetMapping("/admin/n-plus-1")
    public String nPlusOne(Model model) {
        // Query con JOIN FETCH: una sola query per caricare animali + recensioni
        List<Animale> animaliConRecensioni = animaleService.findAllWithRecensioni();

        // Numero totale di recensioni tra tutti gli animali
        int totaleRecensioni = animaliConRecensioni.stream()
            .mapToInt(a -> a.getRecensioni().size())
            .sum();

        // Numero di animali
        int totaleAnimali = animaliConRecensioni.size();

        model.addAttribute("animaliConRecensioni", animaliConRecensioni);
        model.addAttribute("totaleAnimali", totaleAnimali);
        model.addAttribute("totaleRecensioni", totaleRecensioni);

        // Numero di query con N+1 (1 per lista + 1 per ogni animale)
        model.addAttribute("queriesSenzaSoluzione", 1 + totaleAnimali);
        // Numero di query con JOIN FETCH (sempre 1)
        model.addAttribute("queriesConSoluzione", 1);

        return "admin/nPlusOne";
    }
}
