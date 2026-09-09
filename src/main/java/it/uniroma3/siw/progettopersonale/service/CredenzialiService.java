package it.uniroma3.siw.progettopersonale.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.uniroma3.siw.progettopersonale.exception.CodiceVolontarioNonValidoException;
import it.uniroma3.siw.progettopersonale.exception.UsernameGiaUtilizzatoException;
import it.uniroma3.siw.progettopersonale.model.Credenziali;
import it.uniroma3.siw.progettopersonale.model.Ruolo;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.repository.CredenzialiRepository;

/** Gestisce le Credenziali di accesso (username, password, ruolo), come il
 *  CredentialsService mostrato a lezione per l'autenticazione. */
@Service
public class CredenzialiService {

    private static final Logger logger = LoggerFactory.getLogger(CredenzialiService.class);

    /**
     * Parola d'ordine da conoscere per potersi registrare come volontario:
     * evita che chiunque possa iscriversi come dipendente del centro.
     */
    private static final String CODICE_VOLONTARIO = "adozioni";

    private final CredenzialiRepository credenzialiRepository;
    private final PasswordEncoder passwordEncoder;

    public CredenzialiService(CredenzialiRepository credenzialiRepository, PasswordEncoder passwordEncoder) {
        this.credenzialiRepository = credenzialiRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Credenziali getCredenziali(String username) {
        return credenzialiRepository.findByUsername(username).orElse(null);
    }

    /**
     * Registra un nuovo utente con il ruolo indicato (VOLONTARIO o ADOTTANTE), cifrando
     * la password con il PasswordEncoder condiviso con SecurityConfig. Verifica che lo
     * username non sia già in uso e, solo per chi si registra come VOLONTARIO, che sia
     * stato inserito il codice del centro corretto: senza questo controllo chiunque
     * potrebbe iscriversi come "dipendente" e accedere alle funzioni riservate ai
     * volontari. Salvando le Credenziali (cascade ALL) viene salvato anche l'Utente
     * anagrafico collegato.
     */
    @Transactional
    public Credenziali registra(String username, String passwordInChiaro, Ruolo ruolo,
                                 String codiceVolontario, Utente utente) {

        if (credenzialiRepository.findByUsername(username).isPresent()) {
            throw new UsernameGiaUtilizzatoException(username);
        }

        if (ruolo == Ruolo.VOLONTARIO
                && (codiceVolontario == null || !codiceVolontario.trim().equalsIgnoreCase(CODICE_VOLONTARIO))) {
            throw new CodiceVolontarioNonValidoException();
        }

        Credenziali credenziali = new Credenziali();
        credenziali.setUsername(username);
        credenziali.setPassword(passwordEncoder.encode(passwordInChiaro));
        credenziali.setRuolo(ruolo);
        credenziali.setUtente(utente);

        credenziali = credenzialiRepository.save(credenziali);
        logger.info("Nuovo utente registrato: username={}, ruolo={}", username, ruolo);
        return credenziali;
    }
}
