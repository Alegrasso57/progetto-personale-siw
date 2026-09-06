package it.uniroma3.siw.progettopersonale.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "animali")
public class Animale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String specie;

    private String razza;

    private Integer eta;

    private String sesso;

    @Column(length = 2000)
    private String descrizione;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoAnimale stato;

    @OneToMany(mappedBy = "animale", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Turno> turni = new ArrayList<>();

    @OneToMany(mappedBy = "animale", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RichiestaAdozione> richiesteAdozione = new ArrayList<>();

    @OneToMany(mappedBy = "animale", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recensione> recensioni = new ArrayList<>();

    public Animale() {
    }

    public Animale(String nome, String specie, String razza, Integer eta, String sesso, String descrizione) {
        this.nome = nome;
        this.specie = specie;
        this.razza = razza;
        this.eta = eta;
        this.sesso = sesso;
        this.descrizione = descrizione;
        this.stato = StatoAnimale.DISPONIBILE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSpecie() {
        return specie;
    }

    public void setSpecie(String specie) {
        this.specie = specie;
    }

    public String getRazza() {
        return razza;
    }

    public void setRazza(String razza) {
        this.razza = razza;
    }

    public Integer getEta() {
        return eta;
    }

    public void setEta(Integer eta) {
        this.eta = eta;
    }

    public String getSesso() {
        return sesso;
    }

    public void setSesso(String sesso) {
        this.sesso = sesso;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public StatoAnimale getStato() {
        return stato;
    }

    public void setStato(StatoAnimale stato) {
        this.stato = stato;
    }

    public List<Turno> getTurni() {
        return turni;
    }

    public void setTurni(List<Turno> turni) {
        this.turni = turni;
    }

    public List<RichiestaAdozione> getRichiesteAdozione() {
        return richiesteAdozione;
    }

    public void setRichiesteAdozione(List<RichiestaAdozione> richiesteAdozione) {
        this.richiesteAdozione = richiesteAdozione;
    }

    public List<Recensione> getRecensioni() {
        return recensioni;
    }

    public void setRecensioni(List<Recensione> recensioni) {
        this.recensioni = recensioni;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Animale)) return false;
        Animale animale = (Animale) o;
        return id != null && id.equals(animale.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
