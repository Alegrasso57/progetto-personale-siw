package it.uniroma3.siw.progettopersonale.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.progettopersonale.exception.UsernameGiaUtilizzatoException;
import it.uniroma3.siw.progettopersonale.exception.UtenteNonTrovatoException;
import it.uniroma3.siw.progettopersonale.model.Ruolo;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.repository.UtenteRepository;

/**
 * UNICO service per gli utenti: registrazione, login con Google e profilo.
 *
 * Il service e' il posto dove stanno le REGOLE DI BUSINESS (username gia'
 * usato, password cifrata). Il repository fa solo query, il controller solo
 * web: questa e' la separazione in tre livelli.
 */
@Service
public class UtenteService {

    private static final Logger logger = LoggerFactory.getLogger(UtenteService.class);

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;

    public UtenteService(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ==================== lettura ====================

    @Transactional(readOnly = true)
    public Utente findById(Long id) {
        return utenteRepository.findById(id).orElseThrow(() -> new UtenteNonTrovatoException(id));
    }

    /** L'Utente a partire dallo username autenticato (Principal di Spring Security). */
    @Transactional(readOnly = true)
    public Utente findByUsername(String username) {
        return utenteRepository.findByUsername(username)
                .orElseThrow(() -> new UtenteNonTrovatoException(username));
    }

    /** Tutti gli utenti con un certo ruolo. */
    @Transactional(readOnly = true)
    public List<Utente> findByRuolo(Ruolo ruolo) {
        return utenteRepository.findByRuolo(ruolo);
    }

    /** Ricerca per nome o cognome; senza testo restituisce tutti. */
    @Transactional(readOnly = true)
    public List<Utente> findBySearch(String q) {
        if (q == null || q.isBlank()) {
            List<Utente> tutti = new ArrayList<>();
            utenteRepository.findAll().forEach(tutti::add);
            return tutti;
        }
        return utenteRepository.searchByNomeOrCognome(q.trim());
    }

    // ==================== registrazione e accesso ====================

    /**
     * Registra un nuovo utente. La password viene cifrata con BCrypt e il ruolo
     * assegnato e' sempre UTENTE: l'amministratore non si registra dal sito, e'
     * creato all'avvio da DataInitializer.
     */
    @Transactional
    public Utente registra(String username, String passwordInChiaro, String nome, String cognome) {

        if (utenteRepository.existsByUsername(username)) {
            throw new UsernameGiaUtilizzatoException(username);
        }

        Utente utente = new Utente();
        utente.setUsername(username);
        utente.setPassword(passwordEncoder.encode(passwordInChiaro));
        utente.setNome(nome);
        utente.setCognome(cognome);
        utente.setRuolo(Ruolo.UTENTE);

        utente = utenteRepository.save(utente);
        logger.info("Nuovo utente registrato: username={}", username);
        return utente;
    }

    /**
     * Primo accesso con Google: se non esiste gia' un utente con quell'email
     * come username, lo crea con ruolo UTENTE.
     */
    @Transactional
    public Utente trovaOCreaPerOAuth2(String email, String nome, String cognome) {
        return utenteRepository.findByUsername(email).orElseGet(() -> {
            Utente utente = new Utente();
            utente.setUsername(email);
            utente.setNome(nome != null ? nome : "");
            utente.setCognome(cognome != null ? cognome : "");
            utente.setRuolo(Ruolo.UTENTE);
            // Password casuale mai comunicata: si accede solo tramite Google.
            utente.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            logger.info("Nuovo utente creato da login Google: {}", email);
            return utenteRepository.save(utente);
        });
    }

    /** Modifica dei soli dati anagrafici (username, password e ruolo non si toccano da qui). */
    @Transactional
    public Utente aggiornaProfilo(Long id, Utente datiAggiornati) {
        Utente utente = findById(id);
        utente.setNome(datiAggiornati.getNome());
        utente.setCognome(datiAggiornati.getCognome());
        return utenteRepository.save(utente);
    }
}
