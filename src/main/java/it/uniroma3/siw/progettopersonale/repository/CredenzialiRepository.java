package it.uniroma3.siw.progettopersonale.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import it.uniroma3.siw.progettopersonale.model.Credenziali;
import it.uniroma3.siw.progettopersonale.model.Utente;

public interface CredenzialiRepository extends CrudRepository<Credenziali, Long> {

    Optional<Credenziali> findByUsername(String username);

    Optional<Credenziali> findByUtente(Utente utente);
}
