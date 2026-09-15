package it.uniroma3.siw.progettopersonale.model;

/**
 * Ruolo di un Utente. Il nome della costante e' anche il nome dell'authority
 * di Spring Security (vedi SecurityConfig: hasAuthority("ADMIN")).
 */
public enum Ruolo {

    /** Amministratore del rifugio: gestisce gli animali e le richieste di adozione. */
    ADMIN,

    /** Utente normale: richiede adozioni e scrive recensioni sugli animali. */
    UTENTE
}
