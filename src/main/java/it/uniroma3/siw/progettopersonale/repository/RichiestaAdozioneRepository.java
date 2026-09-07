package it.uniroma3.siw.progettopersonale.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.RichiestaAdozione;
import it.uniroma3.siw.progettopersonale.model.StatoRichiesta;
import it.uniroma3.siw.progettopersonale.model.Utente;

public interface RichiestaAdozioneRepository extends JpaRepository<RichiestaAdozione, Long> {

    List<RichiestaAdozione> findByAnimale(Animale animale);

    List<RichiestaAdozione> findByAdottante(Utente adottante);

    List<RichiestaAdozione> findByStato(StatoRichiesta stato);

    List<RichiestaAdozione> findByAnimaleAndStato(Animale animale, StatoRichiesta stato);

    Optional<RichiestaAdozione> findByAdottanteAndAnimaleAndStato(Utente adottante, Animale animale, StatoRichiesta stato);
}
