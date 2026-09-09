package it.uniroma3.siw.progettopersonale.exception;

/** Lanciata quando La richiesta di adozione richiesto non esiste. Gestita centralmente da GlobalExceptionHandler. */
public class RichiestaAdozioneNonTrovataException extends RuntimeException {

    public RichiestaAdozioneNonTrovataException(Long id) {
        super("La richiesta di adozione con ID " + id + " non trovato.");
    }
}
