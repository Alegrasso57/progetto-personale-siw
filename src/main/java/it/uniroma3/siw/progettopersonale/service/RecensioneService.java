package it.uniroma3.siw.progettopersonale.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.Recensione;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.repository.AnimaleRepository;
import it.uniroma3.siw.progettopersonale.repository.RecensioneRepository;
import it.uniroma3.siw.progettopersonale.repository.UtenteRepository;

@Service
public class RecensioneService {

    private final RecensioneRepository recensioneRepository;
    private final AnimaleRepository animaleRepository;
    private final UtenteRepository utenteRepository;

    public RecensioneService(RecensioneRepository recensioneRepository,
                              AnimaleRepository animaleRepository,
                              UtenteRepository utenteRepository) {
        this.recensioneRepository = recensioneRepository;
        this.animaleRepository = animaleRepository;
        this.utenteRepository = utenteRepository;
    }

    @Transactional(readOnly = true)
    public List<Recensione> findByAnimaleId(Long animaleId) {
        Animale animale = animaleRepository.findById(animaleId).orElse(null);
        if (animale == null) {
            throw new IllegalArgumentException("Animale non trovato");
        }
        return recensioneRepository.findByAnimale(animale);
    }

    @Transactional(readOnly = true)
    public Recensione findById(Long id) {
        return recensioneRepository.findById(id).orElse(null);
    }

    private void validaTestoEVoto(String testo, Integer voto) {
        if (testo == null || testo.isBlank()) {
            throw new IllegalArgumentException("Il testo della recensione è obbligatorio");
        }
        if (voto == null || voto < 1 || voto > 5) {
            throw new IllegalArgumentException("Il voto deve essere compreso tra 1 e 5");
        }
    }

    @Transactional
    public Recensione creaRecensione(Long animaleId, Long autoreId, String testo, Integer voto) {

        validaTestoEVoto(testo, voto);

        Animale animale = animaleRepository.findById(animaleId).orElse(null);
        if (animale == null) {
            throw new IllegalArgumentException("Animale non trovato");
        }

        Utente autore = utenteRepository.findById(autoreId).orElse(null);
        if (autore == null) {
            throw new IllegalArgumentException("Utente non trovato");
        }

        if (recensioneRepository.existsByAutoreAndAnimale(autore, animale)) {
            throw new IllegalStateException("Hai già inserito una recensione per questo animale");
        }

        Recensione recensione = new Recensione();
        recensione.setAnimale(animale);
        recensione.setAutore(autore);
        recensione.setTesto(testo);
        recensione.setVoto(voto);
        recensione.setData(LocalDate.now());

        return recensioneRepository.save(recensione);
    }

    @Transactional
    public Recensione modificaRecensione(Long recensioneId, String usernameAutore, String testo, Integer voto) {

        validaTestoEVoto(testo, voto);

        Recensione recensione = recensioneRepository.findById(recensioneId).orElse(null);
        if (recensione == null) {
            throw new IllegalArgumentException("Recensione non trovata");
        }
        if (!recensione.getAutore().getUsername().equals(usernameAutore)) {
            throw new IllegalStateException("Non puoi modificare una recensione di un altro utente");
        }

        recensione.setTesto(testo);
        recensione.setVoto(voto);

        return recensione;
    }

    @Transactional
    public void eliminaRecensione(Long recensioneId, String usernameAutore) {

        Recensione recensione = recensioneRepository.findById(recensioneId).orElse(null);
        if (recensione == null) {
            throw new IllegalArgumentException("Recensione non trovata");
        }
        if (!recensione.getAutore().getUsername().equals(usernameAutore)) {
            throw new IllegalStateException("Non puoi cancellare una recensione di un altro utente");
        }

        recensioneRepository.deleteById(recensioneId);
    }
}