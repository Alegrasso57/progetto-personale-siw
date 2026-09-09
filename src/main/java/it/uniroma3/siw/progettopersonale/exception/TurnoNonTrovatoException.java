package it.uniroma3.siw.progettopersonale.exception;

/** Lanciata quando Il turno richiesto non esiste. Gestita centralmente da GlobalExceptionHandler. */
public class TurnoNonTrovatoException extends RuntimeException {

    public TurnoNonTrovatoException(Long id) {
        super("Il turno con ID " + id + " non trovato.");
    }
}
