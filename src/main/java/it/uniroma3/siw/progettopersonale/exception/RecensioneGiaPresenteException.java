package it.uniroma3.siw.progettopersonale.exception;

/** Lanciata quando un utente tenta di recensire due volte lo stesso amministratore. */
public class RecensioneGiaPresenteException extends RuntimeException {

    public RecensioneGiaPresenteException() {
        super("Hai già scritto una recensione per questo amministratore: puoi modificare quella esistente.");
    }
}
