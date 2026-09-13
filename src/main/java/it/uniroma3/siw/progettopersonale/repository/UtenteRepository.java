package it.uniroma3.siw.progettopersonale.repository;

import org.springframework.data.repository.CrudRepository;
import it.uniroma3.siw.progettopersonale.model.Utente;

public interface UtenteRepository extends CrudRepository<Utente, Long> {
}
