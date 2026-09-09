package it.uniroma3.siw.progettopersonale.repository;

import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.StatoAnimale;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnimaleRepository extends CrudRepository<Animale, Long> {

    List<Animale> findByStato(StatoAnimale stato);

    /** Ricerca per nome o specie, case-insensitive, su tutti gli animali. */
    List<Animale> findByNomeContainingIgnoreCaseOrSpecieContainingIgnoreCase(String nome, String specie);

    /** Ricerca per nome o specie solo tra quelli disponibili. */
    @Query("SELECT a FROM Animale a WHERE a.stato = :stato AND " +
           "(LOWER(a.nome) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(a.specie) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Animale> searchByNomeOrSpecieAndStato(@Param("q") String q, @Param("stato") StatoAnimale stato);

    /**
     * Query con JOIN FETCH per dimostrare la soluzione al problema N+1.
     * Carica in una sola query gli animali insieme alle loro recensioni,
     * evitando N query aggiuntive (una per ogni animale) che si avrebbero
     * accedendo a animale.getRecensioni() fuori dalla transazione.
     */
    @Query("SELECT DISTINCT a FROM Animale a LEFT JOIN FETCH a.recensioni")
    List<Animale> findAllWithRecensioni();
}
