package it.uniroma3.siw.progettopersonale.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.progettopersonale.model.Ruolo;
import it.uniroma3.siw.progettopersonale.model.Utente;

/**
 * UNICO repository per gli utenti: login, registrazione e ricerca.
 *
 * Qui NON ci sono count né ordinamenti: quando servono si aggiungono
 * dichiarando il metodo (es. countByRuolo, findAllByOrderByCognomeAsc),
 * poi lo si espone in UtenteService e infine lo si usa in un controller.
 */
public interface UtenteRepository extends CrudRepository<Utente, Long> {

    /** Usato al login e ovunque serva risalire dall'username autenticato all'Utente. */
    Optional<Utente> findByUsername(String username);

    /** Usato in registrazione per impedire due utenti con lo stesso username. */
    boolean existsByUsername(String username);

    /** Tutti gli utenti con un certo ruolo. */
    List<Utente> findByRuolo(Ruolo ruolo);

    /** Ricerca per nome o cognome, scritta a mano in JPQL. */
    @Query("SELECT u FROM Utente u WHERE "
            + "LOWER(u.nome) LIKE LOWER(CONCAT('%', :q, '%')) "
            + "OR LOWER(u.cognome) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Utente> searchByNomeOrCognome(@Param("q") String q);
}
