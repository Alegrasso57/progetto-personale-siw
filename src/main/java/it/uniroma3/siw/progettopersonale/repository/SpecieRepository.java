package it.uniroma3.siw.progettopersonale.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.StatoAnimale;

/**
 * "Specie" non e' un'entita' con una tabella propria: e' un valore derivato
 * dal campo Animale.specie. Questo repository isola comunque le query che lo
 * calcolano, cosi' SpecieService non deve passare da AnimaleRepository (che
 * resta responsabile solo degli Animale).
 *
 * Estende Repository (il marker base, senza save/delete/findAll) e non
 * CrudRepository: qui servono solo le query in lettura definite qui sotto,
 * non le operazioni CRUD su Animale (quelle restano in AnimaleRepository).
 */
public interface SpecieRepository extends Repository<Animale, Long> {

    /** Specie distinte tra gli animali con un certo stato, in ordine alfabetico crescente (A-Z). */
    @Query("SELECT DISTINCT a.specie FROM Animale a WHERE a.stato = :stato ORDER BY a.specie ASC")
    List<String> findDistinctByStatoOrderByNomeAsc(@Param("stato") StatoAnimale stato);

    /** Stesso elenco, in ordine alfabetico decrescente (Z-A). */
    @Query("SELECT DISTINCT a.specie FROM Animale a WHERE a.stato = :stato ORDER BY a.specie DESC")
    List<String> findDistinctByStatoOrderByNomeDesc(@Param("stato") StatoAnimale stato);
}
