package it.uniroma3.siw.progettopersonale.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.uniroma3.siw.progettopersonale.exception.UtenteNonTrovatoException;
import it.uniroma3.siw.progettopersonale.model.Credenziali;
import it.uniroma3.siw.progettopersonale.model.Ruolo;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.repository.CredenzialiRepository;
import it.uniroma3.siw.progettopersonale.repository.UtenteRepository;

/** Gestisce i dati anagrafici (Utente). Username, password e ruolo appartengono
 *  alle Credenziali e sono gestiti da CredenzialiService. */
@Service
public class UtenteService {

    private static final Logger logger = LoggerFactory.getLogger(UtenteService.class);

    private final UtenteRepository utenteRepository;
    private final CredenzialiRepository credenzialiRepository;

    public UtenteService(UtenteRepository utenteRepository, CredenzialiRepository credenzialiRepository) {
        this.utenteRepository = utenteRepository;
        this.credenzialiRepository = credenzialiRepository;
    }

    @Transactional(readOnly = true)
    public Utente findById(Long id) {
        return utenteRepository.findById(id).orElseThrow(() -> new UtenteNonTrovatoException(id));
    }

    /** Dati anagrafici dell'utente autenticato con questo username (recuperati tramite le sue Credenziali). */
    @Transactional(readOnly = true)
    public Utente findByUsername(String username) {
        return credenzialiRepository.findByUsername(username)
                .map(Credenziali::getUtente)
                .orElse(null);
    }

    /** Ruolo (VOLONTARIO/ADOTTANTE) associato a un utente, tramite le sue Credenziali. */
    @Transactional(readOnly = true)
    public Ruolo findRuolo(Utente utente) {
        return credenzialiRepository.findByUtente(utente)
                .map(Credenziali::getRuolo)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Utente> findVolontari() {
        return credenzialiRepository.findUtentiByRuolo(Ruolo.VOLONTARIO);
    }

    /**
     * Volontari con i turni gia' caricati (soluzione N+1), per la pagina di
     * analisi delle prestazioni in /admin.
     */
    @Transactional(readOnly = true)
    public List<Utente> findVolontariConTurni() {
        return utenteRepository.findByRuoloWithTurni(Ruolo.VOLONTARIO);
    }

    /** Ricerca volontari per nome o cognome. */
    @Transactional(readOnly = true)
    public List<Utente> findVolontariBySearch(String q) {
        if (q == null || q.isBlank()) {
            return findVolontari();
        }
        return credenzialiRepository.searchUtentiByNomeOrCognomeAndRuolo(q.trim(), Ruolo.VOLONTARIO);
    }

    /**
     * Aggiorna i dati anagrafici (nome, cognome) dell'utente indicato.
     * Username, password e ruolo non sono modificabili da qui: sono gestiti dalle
     * Credenziali, non fanno parte del profilo anagrafico.
     */
    @Transactional
    public Utente aggiornaProfilo(Long id, Utente datiAggiornati) {
        Utente utente = findById(id);
        utente.setNome(datiAggiornati.getNome());
        utente.setCognome(datiAggiornati.getCognome());
        Utente salvato = utenteRepository.save(utente);
        logger.info("Profilo aggiornato per utente id={}", id);
        return salvato;
    }
}
