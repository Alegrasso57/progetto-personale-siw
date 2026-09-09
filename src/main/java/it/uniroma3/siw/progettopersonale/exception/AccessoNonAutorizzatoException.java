package it.uniroma3.siw.progettopersonale.exception;

/**
 * Lanciata quando un utente autenticato tenta un'operazione su una risorsa
 * che non gli appartiene (es. modificare la recensione di un altro utente),
 * oppure quando il suo ruolo non consente l'operazione richiesta.
 * Gestita centralmente da GlobalExceptionHandler quando non c'e' una form da
 * ripresentare (es. azioni di elimina/approva), catturata localmente nel
 * controller quando invece serve ripresentare una form con un messaggio.
 */
public class AccessoNonAutorizzatoException extends RuntimeException {

    public AccessoNonAutorizzatoException(String message) {
        super(message);
    }
}
