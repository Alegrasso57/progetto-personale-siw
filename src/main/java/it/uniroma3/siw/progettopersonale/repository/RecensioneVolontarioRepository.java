package it.uniroma3.siw.progettopersonale.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import it.uniroma3.siw.progettopersonale.model.RecensioneVolontario;
import it.uniroma3.siw.progettopersonale.model.Utente;

public interface RecensioneVolontarioRepository extends CrudRepository<RecensioneVolontario, Long> {

    List<RecensioneVolontario> findByVolontario(Utente volontario);

    boolean existsByAutoreAndVolontario(Utente autore, Utente volontario);
}
