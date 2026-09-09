package it.uniroma3.siw.progettopersonale.exception;

/** Lanciata quando L'animale richiesto non esiste. Gestita centralmente da GlobalExceptionHandler. */
public class AnimaleNonTrovatoException extends RuntimeException {

    public AnimaleNonTrovatoException(Long id) {
        super("L'animale con ID " + id + " non trovato.");
    }
}
