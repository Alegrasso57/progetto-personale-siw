package it.uniroma3.siw.progettopersonale.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.StatoAnimale;

/**
 * Qui NON ci sono count né ordinamenti: quando servono si aggiungono
 * dichiarando il metodo (es. countByStato, findAllByOrderByNomeAsc),
 * poi lo si espone in AnimaleService e infine lo si usa in un controller.
 */
public interface AnimaleRepository extends CrudRepository<Animale, Long> {

    List<Animale> findByStato(StatoAnimale stato);

    List<Animale> findByStatoAndSpecieIgnoreCase(StatoAnimale stato, String specie);

    List<Animale> findByNomeContainingIgnoreCaseOrSpecieContainingIgnoreCase(String nome, String specie);

    @Query("SELECT a FROM Animale a WHERE a.stato = :stato AND " +
           "(LOWER(a.nome) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(a.specie) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Animale> searchByNomeOrSpecieAndStato(@Param("q") String q, @Param("stato") StatoAnimale stato);

    /**
     * Tutti gli animali con le loro richieste di adozione caricate in UNA SOLA
     * query (LEFT JOIN FETCH): e' la soluzione al problema N+1 sulla relazione
     * uno-a-molti Animale -> RichiestaAdozione, mostrata nella pagina
     * /admin/analisi-prestazioni.
     *
     * DISTINCT serve perche' il join duplica la riga dell'animale una volta per
     * ogni richiesta collegata: senza, lo stesso Animale comparirebbe piu' volte
     * nella lista.
     */
    @Query("SELECT DISTINCT a FROM Animale a LEFT JOIN FETCH a.richiesteAdozione")
    List<Animale> findTuttiConRichieste();
}
