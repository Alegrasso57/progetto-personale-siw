package it.uniroma3.siw.progettopersonale.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import it.uniroma3.siw.progettopersonale.model.Recensione;
import it.uniroma3.siw.progettopersonale.model.Utente;

/**
 * Recensioni sull'operato degli amministratori.
 *
 * Qui NON ci sono count né ordinamenti: quando servono si aggiungono
 * dichiarando il metodo (es. countByAdmin, findByAdminOrderByVotoDesc),
 * poi lo si espone in RecensioneService e infine lo si usa in un controller.
 */
public interface RecensioneRepository extends CrudRepository<Recensione, Long> {

    /** Tutte le recensioni ricevute da un amministratore. */
    List<Recensione> findByAdmin(Utente admin);

    /** Tutte le recensioni scritte da un utente. */
    List<Recensione> findByAutore(Utente autore);

    /** Un utente non puo' recensire due volte lo stesso amministratore. */
    boolean existsByAutoreAndAdmin(Utente autore, Utente admin);
}
