package it.uniroma3.siw.progettopersonale.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import it.uniroma3.siw.progettopersonale.model.Ruolo;
import it.uniroma3.siw.progettopersonale.model.Utente;

public interface UtenteRepository extends JpaRepository<Utente, Long> {

    Optional<Utente> findByUsername(String username);

    List<Utente> findByRuolo(Ruolo ruolo);

    /** Ricerca volontari per nome o cognome, case-insensitive. */
    @Query("SELECT u FROM Utente u WHERE u.ruolo = :ruolo AND " +
           "(LOWER(u.nome) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(u.cognome) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Utente> searchByNomeOrCognomeAndRuolo(@Param("q") String q, @Param("ruolo") Ruolo ruolo);
}