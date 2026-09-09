package it.uniroma3.siw.progettopersonale.exception;

/** Lanciata quando un adottante ha gia' una richiesta in attesa per lo stesso animale. */
public class RichiestaGiaPresenteException extends RuntimeException {

    public RichiestaGiaPresenteException() {
        super("Hai già una richiesta in attesa per questo animale.");
    }
}
