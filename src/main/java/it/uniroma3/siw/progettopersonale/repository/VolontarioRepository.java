package it.uniroma3.siw.progettopersonale.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.progettopersonale.model.Credenziali;
import it.uniroma3.siw.progettopersonale.model.Ruolo;
import it.uniroma3.siw.progettopersonale.model.Utente;

/**
 * "Volontario" non e' un'entita' a se': e' un Utente le cui Credenziali hanno
 * Ruolo.VOLONTARIO. Questo repository isola le query su questo sottoinsieme,
 * cosi' VolontarioService non deve passare da CredenzialiRepository/UtenteRepository
 * (che restano responsabili di Credenziali e Utente in generale, per qualunque ruolo).
 *
 * Estende Repository (il marker base) e non CrudRepository: qui servono solo
 * le query in lettura definite qui sotto.
 */
public interface VolontarioRepository extends Repository<Credenziali, Long> {

    @Query("SELECT c.utente FROM Credenziali c WHERE c.ruolo = :ruolo")
    List<Utente> findByRuolo(@Param("ruolo") Ruolo ruolo);

    /** Ricerca per nome o cognome, solo tra gli Utente con un certo ruolo. */
    @Query("SELECT c.utente FROM Credenziali c WHERE c.ruolo = :ruolo AND " +
           "(LOWER(c.utente.nome) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.utente.cognome) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Utente> searchByNomeOrCognomeAndRuolo(@Param("q") String q, @Param("ruolo") Ruolo ruolo);

    /** Quanti utenti hanno un certo ruolo (usato per il count "Volontari" nella nav bar/home). */
    long countByRuolo(Ruolo ruolo);

    /**
     * Utenti con un certo ruolo e i turni gia' caricati in una sola query
     * (soluzione al problema N+1), per la pagina di analisi in /admin.
     */
    @Query("SELECT DISTINCT u FROM Utente u LEFT JOIN FETCH u.turni WHERE u.id IN "
            + "(SELECT c.utente.id FROM Credenziali c WHERE c.ruolo = :ruolo)")
    List<Utente> findConTurniByRuolo(@Param("ruolo") Ruolo ruolo);
}
