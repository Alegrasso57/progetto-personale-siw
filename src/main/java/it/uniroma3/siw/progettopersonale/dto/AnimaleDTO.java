package it.uniroma3.siw.progettopersonale.dto;

import it.uniroma3.siw.progettopersonale.model.Animale;

public class AnimaleDTO {

    private final Long id;
    private final String nome;
    private final String specie;
    private final String razza;
    private final Integer eta;
    private final String sesso;
    private final String descrizione;
    private final String stato;

    public AnimaleDTO(Animale animale) {
        this.id = animale.getId();
        this.nome = animale.getNome();
        this.specie = animale.getSpecie();
        this.razza = animale.getRazza();
        this.eta = animale.getEta();
        this.sesso = animale.getSesso();
        this.descrizione = animale.getDescrizione();
        this.stato = animale.getStato().name();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getSpecie() {
        return specie;
    }

    public String getRazza() {
        return razza;
    }

    public Integer getEta() {
        return eta;
    }

    public String getSesso() {
        return sesso;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getStato() {
        return stato;
    }
}