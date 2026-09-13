package it.uniroma3.siw.progettopersonale.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.progettopersonale.model.Ruolo;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.repository.VolontarioRepository;

/** Volontari: gli Utente le cui Credenziali hanno Ruolo.VOLONTARIO. */
@Service
public class VolontarioService {

    private final VolontarioRepository volontarioRepository;

    public VolontarioService(VolontarioRepository volontarioRepository) {
        this.volontarioRepository = volontarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Utente> findTutti() {
        return volontarioRepository.findByRuolo(Ruolo.VOLONTARIO);
    }

    /** Numero totale di volontari registrati: usato per il count nella nav bar/home. */
    @Transactional(readOnly = true)
    public long count() {
        return volontarioRepository.countByRuolo(Ruolo.VOLONTARIO);
    }

    /** Volontari con i turni gia' caricati (soluzione N+1), per la pagina di analisi in /admin. */
    @Transactional(readOnly = true)
    public List<Utente> findTuttiConTurni() {
        return volontarioRepository.findConTurniByRuolo(Ruolo.VOLONTARIO);
    }

    /** Ricerca volontari per nome o cognome. */
    @Transactional(readOnly = true)
    public List<Utente> findBySearch(String q) {
        if (q == null || q.isBlank()) {
            return findTutti();
        }
        return volontarioRepository.searchByNomeOrCognomeAndRuolo(q.trim(), Ruolo.VOLONTARIO);
    }
}
