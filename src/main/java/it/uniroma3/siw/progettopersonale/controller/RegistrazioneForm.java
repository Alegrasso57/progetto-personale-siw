package it.uniroma3.siw.progettopersonale.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import it.uniroma3.siw.progettopersonale.model.Ruolo;

/**
 * Oggetto di comando (non persistito, nessuna annotazione @Entity) per il form
 * di registrazione: raccoglie in un solo oggetto i dati che finiranno su due
 * entita' diverse (Utente e Credenziali), piu' il codice del centro, che non e'
 * un dato persistito da nessuna parte ma serve solo per validare la richiesta.
 */
public class RegistrazioneForm {

    @NotBlank(message = "Lo username è obbligatorio")
    private String username;

    @NotBlank(message = "La password è obbligatoria")
    private String password;

    @NotBlank(message = "Il nome è obbligatorio")
    private String nome;

    @NotBlank(message = "Il cognome è obbligatorio")
    private String cognome;

    @NotNull(message = "Seleziona il ruolo con cui registrarti")
    private Ruolo ruolo;

    private String codiceVolontario;

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

    public Ruolo getRuolo() {
        return ruolo;
    }

    public void setRuolo(Ruolo ruolo) {
        this.ruolo = ruolo;
    }

    public String getCodiceVolontario() {
        return codiceVolontario;
    }

    public void setCodiceVolontario(String codiceVolontario) {
        this.codiceVolontario = codiceVolontario;
    }
}
