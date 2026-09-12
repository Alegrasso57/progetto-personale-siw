package it.uniroma3.siw.progettopersonale.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.StatoAnimale;

public interface AnimaleRepository extends CrudRepository<Animale, Long> {

    List<Animale> findByStato(StatoAnimale stato);


    List<Animale> findByStatoAndSpecieIgnoreCase(StatoAnimale stato, String specie);

    @Query("SELECT DISTINCT a.specie FROM Animale a WHERE a.stato = :stato ORDER BY a.specie")
    List<String> findDistinctSpecieByStato(@Param("stato") StatoAnimale stato);

    List<Animale> findByNomeContainingIgnoreCaseOrSpecieContainingIgnoreCase(String nome, String specie);

    @Query("SELECT a FROM Animale a WHERE a.stato = :stato AND " +
           "(LOWER(a.nome) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(a.specie) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Animale> searchByNomeOrSpecieAndStato(@Param("q") String q, @Param("stato") StatoAnimale stato);
}
