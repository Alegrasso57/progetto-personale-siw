package it.uniroma3.siw.progettopersonale.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import it.uniroma3.siw.progettopersonale.dto.AnimaleDTO;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.service.AnimaleService;

@RestController
@RequestMapping("/api/animali")
public class AnimaleRestController {

    private final AnimaleService animaleService;

    public AnimaleRestController(AnimaleService animaleService) {
        this.animaleService = animaleService;
    }

    @GetMapping
    public List<AnimaleDTO> cercaAnimaliDisponibili(@RequestParam(value = "specie", required = false) String specie) {

        List<Animale> animali = (specie == null || specie.isBlank())
                ? animaleService.findDisponibili()
                : animaleService.findDisponibiliPerSpecie(specie);

        return animali.stream().map(AnimaleDTO::new).collect(Collectors.toList());
    }
}