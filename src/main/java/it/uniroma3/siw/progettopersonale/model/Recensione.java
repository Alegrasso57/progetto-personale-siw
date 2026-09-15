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
 * Recensione sull'OPERATO DI UN AMMINISTRATORE del rifugio: la scrive un
 * utente registrato per dire come si e' trovato con chi gestisce il centro.
 * Non riguarda gli animali.
 *
 * Due relazioni @ManyToOne, entrambe verso Utente ma con significato diverso:
 *   - admin  -> l'amministratore recensito (un Utente con Ruolo.ADMIN)
 *   - autore -> l'utente che ha scritto la recensione
 *
 * Il vincolo di unicita' su (admin_id, autore_id) impedisce a uno stesso
 * utente di recensire due volte lo stesso amministratore: e' la regola che
 * RecensioneService verifica con existsByAutoreAndAdmin prima di salvare.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"admin_id", "autore_id"}))
public class Recensione {

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

    /** L'amministratore di cui si sta recensendo l'operato. */
    @ManyToOne
    private Utente admin;

    /** L'utente che ha scritto la recensione. */
    @ManyToOne
    private Utente autore;

    public Recensione() {
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

    public Utente getAdmin() {
        return admin;
    }

    public void setAdmin(Utente admin) {
        this.admin = admin;
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
        Recensione recensione = (Recensione) obj;
        return id != null && id.equals(recensione.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
