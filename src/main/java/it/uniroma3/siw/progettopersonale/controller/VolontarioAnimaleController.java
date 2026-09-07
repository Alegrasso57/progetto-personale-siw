package it.uniroma3.siw.progettopersonale.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.StatoAnimale;
import it.uniroma3.siw.progettopersonale.service.AnimaleService;

@Controller
public class VolontarioAnimaleController {

    private final AnimaleService animaleService;

    public VolontarioAnimaleController(AnimaleService animaleService) {
        this.animaleService = animaleService;
    }

    @GetMapping("/volontario/animali")
    public String elenco(@RequestParam(value = "cerca", required = false) String cerca, Model model) {
        model.addAttribute("animaleList", animaleService.findAllBySearch(cerca));
        model.addAttribute("cerca", cerca != null ? cerca : "");
        return "volontario/animali";
    }

    @GetMapping("/volontario/animali/nuovo")
    public String formNuovo(Model model) {
        model.addAttribute("animale", new Animale());
        model.addAttribute("statiAnimale", StatoAnimale.values());
        model.addAttribute("erroreValidazione", false);
        return "volontario/animaleForm";
    }

    @GetMapping("/volontario/animali/{id}/modifica")
    public String formModifica(@PathVariable("id") Long id, Model model) {
        Animale animale = animaleService.findById(id);
        if (animale == null) {
            return "redirect:/volontario/animali";
        }
        model.addAttribute("animale", animale);
        model.addAttribute("statiAnimale", StatoAnimale.values());
        model.addAttribute("erroreValidazione", false);
        return "volontario/animaleForm";
    }

    @PostMapping("/volontario/animali")
    public String salva(@Valid @ModelAttribute("animale") Animale animaleForm,
                         BindingResult bindingResult,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("statiAnimale", StatoAnimale.values());
            model.addAttribute("erroreValidazione", true);
            return "volontario/animaleForm";
        }

        Animale animale;
        if (animaleForm.getId() != null) {
            animale = animaleService.findById(animaleForm.getId());
        } else {
            animale = new Animale();
        }

        animale.setNome(animaleForm.getNome());
        animale.setSpecie(animaleForm.getSpecie());
        animale.setRazza(animaleForm.getRazza());
        animale.setEta(animaleForm.getEta());
        animale.setSesso(animaleForm.getSesso());
        animale.setDescrizione(animaleForm.getDescrizione());
        animale.setStato(animaleForm.getStato() != null ? animaleForm.getStato() : StatoAnimale.DISPONIBILE);

        animaleService.save(animale);
        return "redirect:/volontario/animali";
    }

    @PostMapping("/volontario/animali/{id}/elimina")
    public String elimina(@PathVariable("id") Long id) {
        animaleService.deleteById(id);
        return "redirect:/volontario/animali";
    }
}