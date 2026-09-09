package it.uniroma3.siw.progettopersonale.service;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.uniroma3.siw.progettopersonale.exception.AccessoNonAutorizzatoException;
import it.uniroma3.siw.progettopersonale.exception.AnimaleNonTrovatoException;
import it.uniroma3.siw.progettopersonale.exception.RecensioneGiaPresenteException;
import it.uniroma3.siw.progettopersonale.exception.RecensioneNonTrovataException;
import it.uniroma3.siw.progettopersonale.exception.UtenteNonTrovatoException;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.Recensione;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.repository.AnimaleRepository;
import it.uniroma3.siw.progettopersonale.repository.RecensioneRepository;
import it.uniroma3.siw.progettopersonale.repository.UtenteRepository;

@Service
public class RecensioneService {

    private static final Logger logger = LoggerFactory.getLogger(RecensioneService.class);

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
        Animale animale = animaleRepository.findById(animaleId)
                .orElseThrow(() -> new AnimaleNonTrovatoException(animaleId));
        return recensioneRepository.findByAnimale(animale);
    }

    @Transactional(readOnly = true)
    public Recensione findById(Long id) {
        return recensioneRepository.findById(id).orElseThrow(() -> new RecensioneNonTrovataException(id));
    }

    /**
     * Crea una recensione. Testo e voto sono già stati validati dal controller tramite
     * @Valid sull'oggetto Recensione (vincoli dichiarati nell'entità); qui verifichiamo
     * solo la regola di business specifica del caso d'uso: un utente non può recensire
     * due volte lo stesso animale.
     */
    @Transactional
    public Recensione creaRecensione(Long animaleId, Long autoreId, Recensione recensioneForm) {

        Animale animale = animaleRepository.findById(animaleId)
                .orElseThrow(() -> new AnimaleNonTrovatoException(animaleId));
        Utente autore = utenteRepository.findById(autoreId)
                .orElseThrow(() -> new UtenteNonTrovatoException(autoreId));

        if (recensioneRepository.existsByAutoreAndAnimale(autore, animale)) {
            throw new RecensioneGiaPresenteException();
        }

        Recensione recensione = new Recensione();
        recensione.setAnimale(animale);
        recensione.setAutore(autore);
        recensione.setTesto(recensioneForm.getTesto());
        recensione.setVoto(recensioneForm.getVoto());
        recensione.setData(LocalDate.now());

        recensione = recensioneRepository.save(recensione);
        logger.info("Recensione creata: animaleId={}, autoreId={}", animaleId, autoreId);
        return recensione;
    }

    @Transactional
    public Recensione modificaRecensione(Long recensioneId, Long autoreAutenticatoId, Recensione recensioneForm) {

        Recensione recensione = findById(recensioneId);
        if (!recensione.getAutore().getId().equals(autoreAutenticatoId)) {
            throw new AccessoNonAutorizzatoException("Non puoi modificare la recensione di un altro utente.");
        }

        recensione.setTesto(recensioneForm.getTesto());
        recensione.setVoto(recensioneForm.getVoto());
        return recensione;
    }

    @Transactional
    public void eliminaRecensione(Long recensioneId, Long autoreAutenticatoId) {

        Recensione recensione = findById(recensioneId);
        if (!recensione.getAutore().getId().equals(autoreAutenticatoId)) {
            throw new AccessoNonAutorizzatoException("Non puoi cancellare la recensione di un altro utente.");
        }

        recensioneRepository.deleteById(recensioneId);
        logger.info("Recensione eliminata: id={}", recensioneId);
    }
}
