package it.uniroma3.siw.progettopersonale.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Recensione lasciata da un utente registrato sull'operato di un volontario.
 * Stessa struttura di Recensione (sugli animali): risorsa annidata sotto il
 * volontario recensito, come mostrato a lezione per REST (slide "Risorse
 * (naming)", es. "http://localhost/customers/4650/orders").
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"volontario_id", "autore_id"}))
public class RecensioneVolontario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Il testo della recensione è obbligatorio")
    private String testo;

    @NotNull(message = "Il voto è obbligatorio")
    @Min(value = 1, message = "Il voto minimo è 1")
    @Max(value = 5, message = "Il voto massimo è 5")
    private Integer voto;

    private LocalDate data;

    @ManyToOne
    private Utente volontario;

    @ManyToOne
    private Utente autore;

    public RecensioneVolontario() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public Integer getVoto() {
        return voto;
    }

    public void setVoto(Integer voto) {
        this.voto = voto;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public Utente getVolontario() {
        return volontario;
    }

    public void setVolontario(Utente volontario) {
        this.volontario = volontario;
    }

    public Utente getAutore() {
        return autore;
    }

    public void setAutore(Utente autore) {
        this.autore = autore;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RecensioneVolontario that = (RecensioneVolontario) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
