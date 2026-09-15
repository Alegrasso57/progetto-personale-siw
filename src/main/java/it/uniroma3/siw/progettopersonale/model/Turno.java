package it.uniroma3.siw.progettopersonale.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Slot di disponibilita' del rifugio per le visite: una data con una fascia
 * oraria. I turni li inserisce SOLO l'ADMIN (vedi AdminTurnoController);
 * l'utente normale ne sceglie uno libero quando invia una richiesta di
 * adozione.
 *
 * La relazione con RichiestaAdozione e' un @OneToOne il cui lato proprietario
 * (quello che tiene la chiave esterna) sta qui: cosi' "slot libero" si traduce
 * semplicemente in "richiestaAdozione IS NULL", che il repository interroga
 * con findByRichiestaAdozioneIsNull().
 */
@Entity
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull(message = "La data è obbligatoria")
    private LocalDate data;

    @NotNull(message = "L'ora di inizio è obbligatoria")
    private LocalTime oraInizio;

    @NotNull(message = "L'ora di fine è obbligatoria")
    private LocalTime oraFine;

    /** Richiesta che ha prenotato questo slot; null finche' lo slot e' libero. */
    @OneToOne
    @JoinColumn(name = "richiesta_adozione_id", unique = true)
    private RichiestaAdozione richiestaAdozione;

    public Turno() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getOraInizio() {
        return oraInizio;
    }

    public void setOraInizio(LocalTime oraInizio) {
        this.oraInizio = oraInizio;
    }

    public LocalTime getOraFine() {
        return oraFine;
    }

    public void setOraFine(LocalTime oraFine) {
        this.oraFine = oraFine;
    }

    public RichiestaAdozione getRichiestaAdozione() {
        return richiestaAdozione;
    }

    public void setRichiestaAdozione(RichiestaAdozione richiestaAdozione) {
        this.richiestaAdozione = richiestaAdozione;
    }

    /** Comodo nei template: ${turno.libero}. */
    public boolean isLibero() {
        return this.richiestaAdozione == null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Turno turno = (Turno) obj;
        return id != null && id.equals(turno.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
