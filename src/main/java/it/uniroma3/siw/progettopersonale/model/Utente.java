package it.uniroma3.siw.progettopersonale.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

/**
 * Utente registrato del sito. UNICA entita' per la persona: dati anagrafici
 * (nome, cognome) E dati di accesso (username, password, ruolo) stanno qui.
 *
 * Il RUOLO distingue i due tipi di utente:
 *   - Ruolo.ADMIN  -> amministratore del rifugio: inserisce e modifica gli
 *     animali, approva o rifiuta le richieste di adozione, modera le recensioni
 *   - Ruolo.UTENTE -> utente normale: richiede adozioni e scrive recensioni
 */
@Entity
@Table(name = "utente")
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Il nome è obbligatorio")
    private String nome;

    @NotBlank(message = "Il cognome è obbligatorio")
    private String cognome;

    /**
     * Username di accesso (per chi entra con Google e' la sua email).
     * unique = true: due utenti non possono avere lo stesso username.
     *
     * Nota: username, password e ruolo NON hanno annotazioni di Bean
     * Validation, perche' non vengono mai compilati da una form su questa
     * entita': la registrazione passa da RegistrazioneForm (che ha i suoi
     * vincoli) e la modifica del profilo tocca solo nome e cognome.
     */
    @Column(unique = true)
    private String username;

    /** Password cifrata con BCrypt (vedi UtenteService.registra). */
    private String password;

    @Enumerated(EnumType.STRING)
    private Ruolo ruolo;

    public Utente() {
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

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Ruolo getRuolo() {
        return ruolo;
    }

    public void setRuolo(Ruolo ruolo) {
        this.ruolo = ruolo;
    }

    /** Comodo nei template: ${utenteAutenticato.admin}. */
    public boolean isAdmin() {
        return this.ruolo == Ruolo.ADMIN;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Utente utente = (Utente) obj;
        return id != null && id.equals(utente.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
