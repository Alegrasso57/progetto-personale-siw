package it.uniroma3.siw.progettopersonale.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.uniroma3.siw.progettopersonale.exception.AccessoNonAutorizzatoException;
import it.uniroma3.siw.progettopersonale.exception.RecensioneNonTrovataException;
import it.uniroma3.siw.progettopersonale.exception.RecensioneVolontarioGiaPresenteException;
import it.uniroma3.siw.progettopersonale.exception.UtenteNonTrovatoException;
import it.uniroma3.siw.progettopersonale.model.RecensioneVolontario;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.repository.RecensioneVolontarioRepository;
import it.uniroma3.siw.progettopersonale.repository.UtenteRepository;

@Service
public class RecensioneVolontarioService {

    private static final Logger logger = LoggerFactory.getLogger(RecensioneVolontarioService.class);

    private final RecensioneVolontarioRepository recensioneVolontarioRepository;
    private final UtenteRepository utenteRepository;

    public RecensioneVolontarioService(RecensioneVolontarioRepository recensioneVolontarioRepository,
                                        UtenteRepository utenteRepository) {
        this.recensioneVolontarioRepository = recensioneVolontarioRepository;
        this.utenteRepository = utenteRepository;
    }

    @Transactional(readOnly = true)
    public RecensioneVolontario findById(Long id) {
        return recensioneVolontarioRepository.findById(id)
                .orElseThrow(() -> new RecensioneNonTrovataException(id));
    }

    /** Tutte le recensioni presenti, piu' recenti per prime: pagina /recensioni-volontari. */
    @Transactional(readOnly = true)
    public List<RecensioneVolontario> findTutte() {
        List<RecensioneVolontario> risultato = new ArrayList<>();
        recensioneVolontarioRepository.findAll().forEach(risultato::add);
        risultato.sort(Comparator.comparing(RecensioneVolontario::getData).reversed());
        return risultato;
    }

    /**
     * Crea una recensione. Testo e voto sono già stati validati dal controller
     * (voto tra 1 e 5, testo non vuoto); qui verifichiamo solo la regola di
     * business specifica: un utente non può recensire due volte lo stesso volontario.
     */
    @Transactional
    public RecensioneVolontario creaRecensione(Long volontarioId, Long autoreId, RecensioneVolontario recensioneForm) {

        Utente volontario = utenteRepository.findById(volontarioId)
                .orElseThrow(() -> new UtenteNonTrovatoException(volontarioId));
        Utente autore = utenteRepository.findById(autoreId)
                .orElseThrow(() -> new UtenteNonTrovatoException(autoreId));

        if (recensioneVolontarioRepository.existsByAutoreAndVolontario(autore, volontario)) {
            throw new RecensioneVolontarioGiaPresenteException();
        }

        RecensioneVolontario recensione = new RecensioneVolontario();
        recensione.setVolontario(volontario);
        recensione.setAutore(autore);
        recensione.setTesto(recensioneForm.getTesto());
        recensione.setVoto(recensioneForm.getVoto());
        recensione.setData(LocalDate.now());

        recensione = recensioneVolontarioRepository.save(recensione);
        logger.info("Recensione volontario creata: volontarioId={}, autoreId={}", volontarioId, autoreId);
        return recensione;
    }

    @Transactional
    public void eliminaRecensione(Long recensioneId, Long autoreAutenticatoId) {

        RecensioneVolontario recensione = findById(recensioneId);
        if (!recensione.getAutore().getId().equals(autoreAutenticatoId)) {
            throw new AccessoNonAutorizzatoException("Non puoi cancellare la recensione di un altro utente.");
        }

        recensioneVolontarioRepository.deleteById(recensioneId);
        logger.info("Recensione volontario eliminata: id={}", recensioneId);
    }
}
