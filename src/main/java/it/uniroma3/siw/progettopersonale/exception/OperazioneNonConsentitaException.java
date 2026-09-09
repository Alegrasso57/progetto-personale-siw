package it.uniroma3.siw.progettopersonale.exception;

/**
 * Eccezione di dominio generica per violazioni di regole di business legate
 * allo stato corrente dei dati (es. richiesta già gestita, turno non più
 * disponibile, turno sovrapposto). Il service la lancia con un messaggio
 * specifico per il caso; il controller la cattura localmente quando deve
 * ripresentare una form, altrimenti viene gestita da GlobalExceptionHandler.
 */
public class OperazioneNonConsentitaException extends RuntimeException {

    public OperazioneNonConsentitaException(String message) {
        super(message);
    }
}
