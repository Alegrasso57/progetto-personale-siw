package it.uniroma3.siw.progettopersonale.exception;

/** Lanciata quando un utente tenta di inserire una seconda recensione per lo stesso animale. */
public class RecensioneGiaPresenteException extends RuntimeException {

    public RecensioneGiaPresenteException() {
        super("Hai già inserito una recensione per questo animale.");
    }
}
