package it.uniroma3.siw.progettopersonale.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RicercaAnimaliController {

    @GetMapping("/animali/cerca")
    public String cerca() {
        return "animaliCerca";
    }
}