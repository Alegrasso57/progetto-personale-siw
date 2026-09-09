package it.uniroma3.siw.progettopersonale.exception;

/** Lanciata quando chi si registra come volontario non fornisce il codice del centro corretto. */
public class CodiceVolontarioNonValidoException extends RuntimeException {

    public CodiceVolontarioNonValidoException() {
        super("Codice del centro non valido: la registrazione come volontario è riservata ai dipendenti.");
    }
}
