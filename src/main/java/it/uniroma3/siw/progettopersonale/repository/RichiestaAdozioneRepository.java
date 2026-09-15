package it.uniroma3.siw.progettopersonale.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.RichiestaAdozione;
import it.uniroma3.siw.progettopersonale.model.StatoRichiesta;
import it.uniroma3.siw.progettopersonale.model.Utente;

/**
 * Qui NON ci sono count né ordinamenti: quando servono si aggiungono
 * dichiarando il metodo (es. countByStato, findByStatoOrderByDataRichiestaDesc),
 * poi lo si espone in RichiestaAdozioneService e infine lo si usa in un controller.
 */
public interface RichiestaAdozioneRepository extends CrudRepository<RichiestaAdozione, Long> {

    List<RichiestaAdozione> findByAnimale(Animale animale);

    List<RichiestaAdozione> findByAdottante(Utente adottante);

    List<RichiestaAdozione> findByStato(StatoRichiesta stato);

    List<RichiestaAdozione> findByAnimaleAndStato(Animale animale, StatoRichiesta stato);

    Optional<RichiestaAdozione> findByAdottanteAndAnimaleAndStato(Utente adottante, Animale animale, StatoRichiesta stato);
}
