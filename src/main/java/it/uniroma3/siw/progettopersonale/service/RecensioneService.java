package it.uniroma3.siw.progettopersonale.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.uniroma3.siw.progettopersonale.exception.AccessoNonAutorizzatoException;
import it.uniroma3.siw.progettopersonale.exception.RecensioneGiaPresenteException;
import it.uniroma3.siw.progettopersonale.exception.RecensioneNonTrovataException;
import it.uniroma3.siw.progettopersonale.exception.UtenteNonTrovatoException;
import it.uniroma3.siw.progettopersonale.model.Recensione;
import it.uniroma3.siw.progettopersonale.model.Ruolo;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.repository.RecensioneRepository;
import it.uniroma3.siw.progettopersonale.repository.UtenteRepository;

/**
 * Recensioni sull'operato degli amministratori del rifugio.
 *
 * REGOLE DI BUSINESS, tutte concentrate qui:
 *   - scrive solo un utente REGISTRATO con ruolo UTENTE; un ADMIN puo'
 *     leggere le recensioni ma non scriverne
 *   - si puo' recensire solo un Utente che e' davvero un ADMIN
 *   - un utente non puo' recensire due volte lo stesso amministratore
 *   - modifica ed eliminazione sono riservate all'autore della recensione
 */
@Service
public class RecensioneService {

    private static final Logger logger = LoggerFactory.getLogger(RecensioneService.class);

    private final RecensioneRepository recensioneRepository;
    private final UtenteRepository utenteRepository;

    public RecensioneService(RecensioneRepository recensioneRepository,
                              UtenteRepository utenteRepository) {
        this.recensioneRepository = recensioneRepository;
        this.utenteRepository = utenteRepository;
    }

    @Transactional(readOnly = true)
    public Recensione findById(Long id) {
        return recensioneRepository.findById(id).orElseThrow(() -> new RecensioneNonTrovataException(id));
    }

    /** Tutte le recensioni pubblicate: e' l'elenco della pagina /recensioni. */
    @Transactional(readOnly = true)
    public List<Recensione> findTutte() {
        List<Recensione> risultato = new ArrayList<>();
        recensioneRepository.findAll().forEach(risultato::add);
        return risultato;
    }

    /** Le recensioni ricevute da un amministratore. */
    @Transactional(readOnly = true)
    public List<Recensione> findByAdmin(Utente admin) {
        return recensioneRepository.findByAdmin(admin);
    }

    private Utente utente(Long id) {
        return utenteRepository.findById(id).orElseThrow(() -> new UtenteNonTrovatoException(id));
    }

    /**
     * Un utente registrato scrive una recensione sull'operato di un admin.
     * Testo e voto sono gia' stati validati dal controller con @Valid.
     */
    @Transactional
    public Recensione creaRecensione(Long adminId, Long autoreId, Recensione recensioneForm) {

        Utente admin = utente(adminId);
        Utente autore = utente(autoreId);

        if (admin.getRuolo() != Ruolo.ADMIN) {
            throw new AccessoNonAutorizzatoException("Puoi recensire solo l'operato di un amministratore.");
        }
        if (autore.getRuolo() != Ruolo.UTENTE) {
            throw new AccessoNonAutorizzatoException(
                    "Gli amministratori possono leggere le recensioni ma non scriverne.");
        }
        if (recensioneRepository.existsByAutoreAndAdmin(autore, admin)) {
            throw new RecensioneGiaPresenteException();
        }

        Recensione recensione = new Recensione();
        recensione.setAdmin(admin);
        recensione.setAutore(autore);
        recensione.setTesto(recensioneForm.getTesto());
        recensione.setVoto(recensioneForm.getVoto());
        recensione.setData(LocalDate.now());

        recensione = recensioneRepository.save(recensione);
        logger.info("Recensione creata: adminId={}, autoreId={}", adminId, autoreId);
        return recensione;
    }

    /** Solo l'autore puo' modificare la propria recensione. */
    @Transactional
    public Recensione modificaRecensione(Long recensioneId, Long autoreAutenticatoId, Recensione recensioneForm) {

        Recensione recensione = findById(recensioneId);
        if (!recensione.getAutore().getId().equals(autoreAutenticatoId)) {
            throw new AccessoNonAutorizzatoException("Non puoi modificare la recensione di un altro utente.");
        }

        recensione.setTesto(recensioneForm.getTesto());
        recensione.setVoto(recensioneForm.getVoto());
        logger.info("Recensione modificata: id={}", recensioneId);
        return recensione;
    }

    /** Solo l'autore puo' eliminare la propria recensione. */
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
