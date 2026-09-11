package it.uniroma3.siw.progettopersonale.dto;

import it.uniroma3.siw.progettopersonale.model.Animale;

/**
 * Solo i campi di un Animale che servono alla barra di ricerca in React
 * (vedi frontend-ricerca-animali/) e alla card che lo rappresenta in pagina.
 * Una classe JavaBean "normale" (non un record) apposta: sia la
 * serializzazione JSON di Thymeleaf (th:inline="javascript", usata per i
 * risultati iniziali) sia Jackson (usata dall'endpoint REST sotto) leggono i
 * getter in stile "getXxx()"/"isXxx()", che un record non genera. Non include
 * le collezioni (turni, richiesteAdozione, recensioni) dell'entita' Animale:
 * sono LAZY e non servono qui, evitando query in piu' o errori di
 * serializzazione.
 */
public class AnimaleRicercaDTO {

    private final Long id;
    private final String nome;
    private final String specie;
    private final String razza;
    private final Integer eta;
    private final String sesso;
    private final String descrizione;
    private final String stato;

    public AnimaleRicercaDTO(Long id, String nome, String specie, String razza, Integer eta,
                              String sesso, String descrizione, String stato) {
        this.id = id;
        this.nome = nome;
        this.specie = specie;
        this.razza = razza;
        this.eta = eta;
        this.sesso = sesso;
        this.descrizione = descrizione;
        this.stato = stato;
    }

    public static AnimaleRicercaDTO from(Animale animale) {
        return new AnimaleRicercaDTO(
                animale.getId(),
                animale.getNome(),
                animale.getSpecie(),
                animale.getRazza(),
                animale.getEta(),
                animale.getSesso(),
                animale.getDescrizione(),
                animale.getStato().name());
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
