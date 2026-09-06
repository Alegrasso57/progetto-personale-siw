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
    public Utente findById(Long id) {
        return utenteRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Utente> findVolontari() {
        return utenteRepository.findByRuolo(Ruolo.VOLONTARIO);
    }

    /**
     * Registra un nuovo utente con il ruolo indicato (VOLONTARIO o ADOTTANTE), cifrando la password.
     * Verifica che lo username non sia già in uso.
     */
    @Transactional
    public Utente registra(String username, String passwordInChiaro, String nome, String cognome, Ruolo ruolo) {

        Utente esistente = utenteRepository.findByUsername(username).orElse(null);
        if (esistente != null) {
            throw new IllegalStateException("Username già in uso");
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