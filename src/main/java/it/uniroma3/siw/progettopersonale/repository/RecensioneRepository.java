package it.uniroma3.siw.progettopersonale.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.Recensione;
import it.uniroma3.siw.progettopersonale.model.Utente;

public interface RecensioneRepository extends CrudRepository<Recensione, Long> {

    List<Recensione> findByAnimale(Animale animale);

    boolean existsByAutoreAndAnimale(Utente autore, Animale animale);
}
