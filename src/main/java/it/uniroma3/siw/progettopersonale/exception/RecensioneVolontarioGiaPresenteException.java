package it.uniroma3.siw.progettopersonale.exception;

/** Lanciata quando un utente tenta di inserire una seconda recensione per lo stesso volontario. */
public class RecensioneVolontarioGiaPresenteException extends RuntimeException {

    public RecensioneVolontarioGiaPresenteException() {
        super("Hai già inserito una recensione per questo volontario.");
    }
}
