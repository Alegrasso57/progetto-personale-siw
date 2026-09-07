package it.uniroma3.siw.progettopersonale;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import it.uniroma3.siw.progettopersonale.repository.AnimaleRepository;
import it.uniroma3.siw.progettopersonale.repository.RecensioneRepository;
import it.uniroma3.siw.progettopersonale.repository.RichiestaAdozioneRepository;
import it.uniroma3.siw.progettopersonale.repository.TurnoRepository;
import it.uniroma3.siw.progettopersonale.repository.UtenteRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UtenteRepository utenteRepository;
    private final AnimaleRepository animaleRepository;
    private final TurnoRepository turnoRepository;
    private final RichiestaAdozioneRepository richiestaAdozioneRepository;
    private final RecensioneRepository recensioneRepository;

    public DataInitializer(UtenteRepository utenteRepository,
                            AnimaleRepository animaleRepository,
                            TurnoRepository turnoRepository,
                            RichiestaAdozioneRepository richiestaAdozioneRepository,
                            RecensioneRepository recensioneRepository) {
        this.utenteRepository = utenteRepository;
        this.animaleRepository = animaleRepository;
        this.turnoRepository = turnoRepository;
        this.richiestaAdozioneRepository = richiestaAdozioneRepository;
        this.recensioneRepository = recensioneRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        // Questa classe inizialmente inseriva dati di prova (volontari, animali,
        // turni, richieste, recensioni) per poter sviluppare e testare l'applicazione
        // senza dover compilare tutto a mano. Ora che l'app e' pronta per l'uso reale,
        // il metodo NON inserisce piu' nulla: si limita a ripulire, una sola volta,
        // gli eventuali dati di prova rimasti da una vecchia esecuzione, cosi' che
        // l'utente possa inserire i propri animali/volontari/ecc. dall'interfaccia.
        //
        // Il controllo cerca l'utente "marco.volontario", marcatore dei vecchi dati
        // di prova: se non esiste (perche' non e' mai stato creato, o perche' la
        // pulizia e' gia' stata eseguita in un avvio precedente) il metodo non fa
        // nulla, quindi resta sicuro anche sui riavvii successivi e non tocca mai
        // i dati reali inseriti dall'utente.
        boolean datiDiProvaPresenti = utenteRepository.findByUsername("marco.volontario").isPresent();
        if (!datiDiProvaPresenti) {
            return;
        }

        recensioneRepository.deleteAll();
        richiestaAdozioneRepository.deleteAll();
        turnoRepository.deleteAll();
        animaleRepository.deleteAll();
        utenteRepository.deleteAll();

        System.out.println(">>> Dati di prova rimossi: il database e' ora vuoto.");
    }
}
