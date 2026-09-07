package it.uniroma3.siw.progettopersonale.service;

import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.uniroma3.siw.progettopersonale.model.Ruolo;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.repository.UtenteRepository;

@Service
public class UtenteService {

    /**
     * Parola d'ordine da conoscere per potersi registrare come volontario:
     * evita che chiunque possa iscriversi come dipendente del centro.
     */
    private static final String CODICE_VOLONTARIO = "adozioni";

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;

    public UtenteService(UtenteRepository utenteRepository) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional(readOnly = true)
    public Utente findByUsername(String username) {
        return utenteRepository.findByUsername(username).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Utente> findVolontari() {
        return utenteRepository.findByRuolo(Ruolo.VOLONTARIO);
    }

    /** Ricerca volontari per nome o cognome. */
    @Transactional(readOnly = true)
    public List<Utente> findVolontariBySearch(String q) {
        if (q == null || q.isBlank()) {
            return findVolontari();
        }
        return utenteRepository.searchByNomeOrCognomeAndRuolo(q.trim(), Ruolo.VOLONTARIO);
    }

    /**
     * Aggiorna i dati anagrafici (nome, cognome, email, telefono) dell'utente indicato.
     * Username, password e ruolo non sono modificabili da qui: lo username è l'identificativo
     * di accesso (cambiarlo romperebbe l'autenticazione già in corso) e il ruolo è una scelta
     * fatta in fase di registrazione, non un dato di profilo.
     */
    @Transactional
    public Utente aggiornaProfilo(Long id, String nome, String cognome, String email, String telefono) {
        Utente utente = utenteRepository.findById(id).orElse(null);
        if (utente == null) {
            throw new IllegalArgumentException("Utente non trovato");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Il nome è obbligatorio");
        }
        if (cognome == null || cognome.isBlank()) {
            throw new IllegalArgumentException("Il cognome è obbligatorio");
        }
        utente.setNome(nome);
        utente.setCognome(cognome);
        utente.setEmail(email);
        utente.setTelefono(telefono);
        return utenteRepository.save(utente);
    }

    /**
     * Registra un nuovo utente con il ruolo indicato (VOLONTARIO o ADOTTANTE), cifrando la password.
     * Verifica che lo username non sia già in uso e, solo per chi si registra come VOLONTARIO,
     * che sia stato inserito il codice del centro corretto: senza questo controllo chiunque
     * potrebbe iscriversi come "dipendente" e accedere alle funzioni riservate ai volontari.
     */
    @Transactional
    public Utente registra(String username, String passwordInChiaro, String nome, String cognome,
                            Ruolo ruolo, String codiceVolontario) {

        Utente esistente = utenteRepository.findByUsername(username).orElse(null);
        if (esistente != null) {
            throw new IllegalStateException("Username già in uso");
        }

        if (ruolo == Ruolo.VOLONTARIO) {
            if (codiceVolontario == null || !codiceVolontario.trim().equalsIgnoreCase(CODICE_VOLONTARIO)) {
                throw new IllegalStateException("Codice del centro non valido: la registrazione come volontario è riservata ai dipendenti");
            }
        }

        Utente utente = new Utente();
        utente.setUsername(username);
        utente.setPassword(passwordEncoder.encode(passwordInChiaro));
        utente.setNome(nome);
        utente.setCognome(cognome);
        utente.setRuolo(ruolo);

        return utenteRepository.save(utente);
    }
}