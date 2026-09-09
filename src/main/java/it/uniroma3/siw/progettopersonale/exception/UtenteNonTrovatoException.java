package it.uniroma3.siw.progettopersonale.exception;

/** Lanciata quando L'utente richiesto non esiste. Gestita centralmente da GlobalExceptionHandler. */
public class UtenteNonTrovatoException extends RuntimeException {

    public UtenteNonTrovatoException(Long id) {
        super("L'utente con ID " + id + " non trovato.");
    }
}
