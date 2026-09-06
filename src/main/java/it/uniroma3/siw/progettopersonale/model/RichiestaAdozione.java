package it.uniroma3.siw.progettopersonale.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
public class RichiestaAdozione {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull(message = "La data della richiesta è obbligatoria")
    private LocalDate dataRichiesta;

    @Enumerated(EnumType.STRING)
    private StatoRichiesta stato = StatoRichiesta.IN_ATTESA;

    private String motivazione;

    @ManyToOne
    private Utente adottante;

    @ManyToOne
    private Animale animale;

    public RichiestaAdozione() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDataRichiesta() {
        return dataRichiesta;
    }

    public void setDataRichiesta(LocalDate dataRichiesta) {
        this.dataRichiesta = dataRichiesta;
    }

    public StatoRichiesta getStato() {
        return stato;
    }

    public void setStato(StatoRichiesta stato) {
        this.stato = stato;
    }

    public String getMotivazione() {
        return motivazione;
    }

    public void setMotivazione(String motivazione) {
        this.motivazione = motivazione;
    }

    public Utente getAdottante() {
        return adottante;
    }

    public void setAdottante(Utente adottante) {
        this.adottante = adottante;
    }

    public Animale getAnimale() {
        return animale;
    }

    public void setAnimale(Animale animale) {
        this.animale = animale;
    }
}