package it.uniroma3.siw.progettopersonale.exception;

/** Lanciata quando si tenta di richiedere l'adozione di un animale non disponibile. */
public class AnimaleNonDisponibileException extends RuntimeException {

    public AnimaleNonDisponibileException() {
        super("L'animale non è al momento disponibile per l'adozione.");
    }
}
