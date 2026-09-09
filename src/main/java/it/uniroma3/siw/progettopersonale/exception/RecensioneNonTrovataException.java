package it.uniroma3.siw.progettopersonale.exception;

/** Lanciata quando La recensione richiesto non esiste. Gestita centralmente da GlobalExceptionHandler. */
public class RecensioneNonTrovataException extends RuntimeException {

    public RecensioneNonTrovataException(Long id) {
        super("La recensione con ID " + id + " non trovato.");
    }
}
