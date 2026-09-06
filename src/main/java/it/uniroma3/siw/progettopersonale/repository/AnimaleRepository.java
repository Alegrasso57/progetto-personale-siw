package it.uniroma3.siw.progettopersonale.repository;

import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.StatoAnimale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimaleRepository extends JpaRepository<Animale, Long> {

    List<Animale> findByStato(StatoAnimale stato);

    List<Animale> findBySpecieIgnoreCaseAndStato(String specie, StatoAnimale stato);
}
