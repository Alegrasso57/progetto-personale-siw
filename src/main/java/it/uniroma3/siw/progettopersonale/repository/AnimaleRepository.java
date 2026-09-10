package it.uniroma3.siw.progettopersonale.repository;

import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.StatoAnimale;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnimaleRepository extends CrudRepository<Animale, Long> {

    List<Animale> findByStato(StatoAnimale stato);

    /** Animali disponibili di una specie esatta (per la navigazione per specie). */
    List<Animale> findByStatoAndSpecieIgnoreCase(StatoAnimale stato, String specie);

    /** Elenco delle specie (senza duplicati) tra gli animali disponibili, per popolare menu e home. */
    @Query("SELECT DISTINCT a.specie FROM Animale a WHERE a.stato = :stato ORDER BY a.specie")
    List<String> findDistinctSpecieByStato(@Param("stato") StatoAnimale stato);

    /** Ricerca per nome o specie, case-insensitive, su tutti gli animali. */
    List<Animale> findByNomeContainingIgnoreCaseOrSpecieContainingIgnoreCase(String nome, String specie);

    /** Ricerca per nome o specie solo tra quelli disponibili. */
    @Query("SELECT a FROM Animale a WHERE a.stato = :stato AND " +
           "(LOWER(a.nome) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(a.specie) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Animale> searchByNomeOrSpecieAndStato(@Param("q") String q, @Param("stato") StatoAnimale stato);

}
