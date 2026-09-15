package it.uniroma3.siw.progettopersonale.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Richiesta di adozione inviata da un Utente per un Animale, con lo slot
 * orario scelto fra quelli messi a disposizione dall'admin.
 * L'admin la approva o la rifiuta (vedi RichiestaAdozioneService).
 */
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

    /** L'utente che ha inviato la richiesta. */
    @ManyToOne
    private Utente adottante;

    @ManyToOne
    private Animale animale;

    /**
     * Lo slot prenotato per la visita. Lato INVERSO della relazione: la chiave
     * esterna sta su Turno (vedi Turno.richiestaAdozione), qui serve solo a
     * leggere comodamente il turno dal lato della richiesta, per esempio nei
     * template con ${richiesta.turno.data}.
     */
    @OneToOne(mappedBy = "richiestaAdozione")
    private Turno turno;

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

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RichiestaAdozione that = (RichiestaAdozione) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
