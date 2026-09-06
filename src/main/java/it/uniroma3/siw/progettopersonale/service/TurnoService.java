package it.uniroma3.siw.progettopersonale.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.Ruolo;
import it.uniroma3.siw.progettopersonale.model.Turno;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.repository.AnimaleRepository;
import it.uniroma3.siw.progettopersonale.repository.TurnoRepository;
import it.uniroma3.siw.progettopersonale.repository.UtenteRepository;

@Service
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final AnimaleRepository animaleRepository;
    private final UtenteRepository utenteRepository;

    public TurnoService(TurnoRepository turnoRepository,
                         AnimaleRepository animaleRepository,
                         UtenteRepository utenteRepository) {
        this.turnoRepository = turnoRepository;
        this.animaleRepository = animaleRepository;
        this.utenteRepository = utenteRepository;
    }

    @Transactional(readOnly = true)
    public List<Turno> findAll() {
        return turnoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Turno> findByData(LocalDate data) {
        return turnoRepository.findByDataOrderByOraInizioAsc(data);
    }

    @Transactional(readOnly = true)
    public List<Turno> findByAnimaleId(Long animaleId) {
        Animale animale = animaleRepository.findById(animaleId).orElse(null);
        if (animale == null) {
            throw new IllegalArgumentException("Animale non trovato");
        }
        return turnoRepository.findByAnimale(animale);
    }

    @Transactional(readOnly = true)
    public Turno findById(Long id) {
        return turnoRepository.findById(id).orElse(null);
    }

    @Transactional
    public Turno creaTurno(Long volontarioId, Long animaleId, LocalDate data,
                            LocalTime oraInizio, LocalTime oraFine, String note) {

        Utente volontario = utenteRepository.findById(volontarioId).orElse(null);
        if (volontario == null) {
            throw new IllegalArgumentException("Volontario non trovato");
        }
        if (volontario.getRuolo() != Ruolo.VOLONTARIO) {
            throw new IllegalStateException("L'utente selezionato non è un volontario");
        }

        Animale animale = animaleRepository.findById(animaleId).orElse(null);
        if (animale == null) {
            throw new IllegalArgumentException("Animale non trovato");
        }

        if (!oraFine.isAfter(oraInizio)) {
            throw new IllegalArgumentException("L'ora di fine deve essere successiva all'ora di inizio");
        }

        boolean sovrapposto = turnoRepository.findByVolontarioAndData(volontario, data).stream()
                .anyMatch(t -> t.getOraInizio().isBefore(oraFine) && oraInizio.isBefore(t.getOraFine()));
        if (sovrapposto) {
            throw new IllegalStateException("Il volontario ha già un turno che si sovrappone in quella fascia oraria");
        }

        Turno turno = new Turno();
        turno.setVolontario(volontario);
        turno.setAnimale(animale);
        turno.setData(data);
        turno.setOraInizio(oraInizio);
        turno.setOraFine(oraFine);
        turno.setNote(note);

        return turnoRepository.save(turno);
    }

    @Transactional
    public void deleteById(Long id) {
        turnoRepository.deleteById(id);
    }
}