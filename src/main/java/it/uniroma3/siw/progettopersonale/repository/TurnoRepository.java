package it.uniroma3.siw.progettopersonale.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.progettopersonale.model.RichiestaAdozione;
import it.uniroma3.siw.progettopersonale.model.Turno;

public interface TurnoRepository extends CrudRepository<Turno, Long> {

    /** Tutti i turni di una certa data: serve al controllo delle sovrapposizioni. */
    List<Turno> findByData(LocalDate data);

    /** Slot ancora liberi: quelli che nessuna richiesta ha prenotato. */
    List<Turno> findByRichiestaAdozioneIsNull();

    /** Lo slot prenotato da una certa richiesta (vuoto se non ne ha). */
    Optional<Turno> findByRichiestaAdozione(RichiestaAdozione richiestaAdozione);
}
