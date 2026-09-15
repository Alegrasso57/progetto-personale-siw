package it.uniroma3.siw.progettopersonale.controller;

import jakarta.validation.constraints.NotBlank;

/**
 * Oggetto di comando (non persistito, nessuna annotazione @Entity) per il form
 * di registrazione.
 *
 * Esiste separato dall'entita' Utente perche' qui la password e' obbligatoria
 * (@NotBlank), mentre sull'entita' non puo' esserlo: la form di modifica del
 * profilo invia solo nome e cognome, e un vincolo sulla password la farebbe
 * fallire. Il ruolo non c'e': chi si registra e' sempre un UTENTE normale,
 * l'amministratore viene creato all'avvio da DataInitializer.
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
}
