package it.uniroma3.siw.progettopersonale.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import it.uniroma3.siw.progettopersonale.model.Ruolo;
import it.uniroma3.siw.progettopersonale.model.Utente;

public interface UtenteRepository extends CrudRepository<Utente, Long> {

    /**
     * Volontari con i turni gia' caricati in una sola query (soluzione al
     * problema N+1): il ruolo vive nelle Credenziali, non su Utente, quindi
     * il filtro passa per una sottoquery su Credenziali.
     */
    @Query("SELECT DISTINCT u FROM Utente u LEFT JOIN FETCH u.turni WHERE u.id IN "
            + "(SELECT c.utente.id FROM Credenziali c WHERE c.ruolo = :ruolo)")
    List<Utente> findByRuoloWithTurni(@Param("ruolo") Ruolo ruolo);
}
