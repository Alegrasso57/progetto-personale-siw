package it.uniroma3.siw.progettopersonale.exception;

/** Lanciata in registrazione quando lo username scelto e' gia' in uso. */
public class UsernameGiaUtilizzatoException extends RuntimeException {

    public UsernameGiaUtilizzatoException(String username) {
        super("Lo username '" + username + "' è già in uso.");
    }
}
