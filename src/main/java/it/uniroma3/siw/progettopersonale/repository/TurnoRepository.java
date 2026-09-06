package it.uniroma3.siw.progettopersonale.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.Turno;
import it.uniroma3.siw.progettopersonale.model.Utente;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    List<Turno> findByAnimale(Animale animale);

    List<Turno> findByVolontario(Utente volontario);

    List<Turno> findByDataOrderByOraInizioAsc(LocalDate data);

    List<Turno> findByVolontarioAndData(Utente volontario, LocalDate data);
}