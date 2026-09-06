package it.uniroma3.siw.progettopersonale.model;

/**
 * Ruolo di un Utente registrato.
 * I visitatori anonimi (non registrati) non hanno una riga Utente: possono solo consultare le pagine pubbliche.
 */
public enum Ruolo {
    VOLONTARIO,
    ADOTTANTE
}
