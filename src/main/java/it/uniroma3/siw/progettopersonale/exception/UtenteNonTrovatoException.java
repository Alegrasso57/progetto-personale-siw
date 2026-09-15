package it.uniroma3.siw.progettopersonale.exception;

/** Lanciata quando l'utente richiesto non esiste. Gestita centralmente da GlobalExceptionHandler. */
public class UtenteNonTrovatoException extends RuntimeException {

    public UtenteNonTrovatoException(Long id) {
        super("L'utente con ID " + id + " non è stato trovato.");
    }

    public UtenteNonTrovatoException(String username) {
        super("L'utente '" + username + "' non è stato trovato.");
    }
}
