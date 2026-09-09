package it.uniroma3.siw.progettopersonale.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import it.uniroma3.siw.progettopersonale.model.Credenziali;
import it.uniroma3.siw.progettopersonale.model.Ruolo;
import it.uniroma3.siw.progettopersonale.model.Utente;

public interface CredenzialiRepository extends CrudRepository<Credenziali, Long> {

    Optional<Credenziali> findByUsername(String username);

    Optional<Credenziali> findByUtente(Utente utente);

    /** Elenco degli Utente (dati anagrafici) che hanno Credenziali con un certo ruolo. */
    @Query("SELECT c.utente FROM Credenziali c WHERE c.ruolo = :ruolo")
    List<Utente> findUtentiByRuolo(@Param("ruolo") Ruolo ruolo);

    /** Ricerca per nome o cognome, tra gli Utente con un certo ruolo (dot-notation su Credenziali.utente). */
    @Query("SELECT c.utente FROM Credenziali c WHERE c.ruolo = :ruolo AND " +
           "(LOWER(c.utente.nome) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.utente.cognome) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Utente> searchUtentiByNomeOrCognomeAndRuolo(@Param("q") String q, @Param("ruolo") Ruolo ruolo);
}
