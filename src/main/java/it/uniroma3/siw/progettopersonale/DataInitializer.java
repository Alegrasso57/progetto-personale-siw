package it.uniroma3.siw.progettopersonale;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import it.uniroma3.siw.progettopersonale.model.Animale;
import it.uniroma3.siw.progettopersonale.model.Recensione;
import it.uniroma3.siw.progettopersonale.model.RichiestaAdozione;
import it.uniroma3.siw.progettopersonale.model.Ruolo;
import it.uniroma3.siw.progettopersonale.model.StatoAnimale;
import it.uniroma3.siw.progettopersonale.model.StatoRichiesta;
import it.uniroma3.siw.progettopersonale.model.Turno;
import it.uniroma3.siw.progettopersonale.model.Utente;
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

        // Evita di duplicare i dati ad ogni riavvio dell'applicazione
        if (animaleRepository.count() > 0) {
            return;
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Volontari
        Utente marco = new Utente();
        marco.setUsername("marco.volontario");
        marco.setPassword(encoder.encode("volontario123"));
        marco.setNome("Marco");
        marco.setCognome("Bianchi");
        marco.setEmail("marco.bianchi@rifugioamico.it");
        marco.setTelefono("333 1234567");
        marco.setRuolo(Ruolo.VOLONTARIO);
        utenteRepository.save(marco);

        Utente giulia = new Utente();
        giulia.setUsername("giulia.volontaria");
        giulia.setPassword(encoder.encode("volontario123"));
        giulia.setNome("Giulia");
        giulia.setCognome("Rossi");
        giulia.setEmail("giulia.rossi@rifugioamico.it");
        giulia.setTelefono("333 7654321");
        giulia.setRuolo(Ruolo.VOLONTARIO);
        utenteRepository.save(giulia);

        // Adottanti
        Utente luca = new Utente();
        luca.setUsername("luca.adottante");
        luca.setPassword(encoder.encode("adottante123"));
        luca.setNome("Luca");
        luca.setCognome("Verdi");
        luca.setEmail("luca.verdi@example.com");
        luca.setRuolo(Ruolo.ADOTTANTE);
        utenteRepository.save(luca);

        Utente sara = new Utente();
        sara.setUsername("sara.adottante");
        sara.setPassword(encoder.encode("adottante123"));
        sara.setNome("Sara");
        sara.setCognome("Neri");
        sara.setEmail("sara.neri@example.com");
        sara.setRuolo(Ruolo.ADOTTANTE);
        utenteRepository.save(sara);

        // Animali
        Animale fido = new Animale("Fido", "Cane", "Meticcio", 3, "Maschio",
                "Un cane dolcissimo, ama giocare con la palla e va d'accordo con tutti.");
        animaleRepository.save(fido);

        Animale luna = new Animale("Luna", "Gatto", "Europeo", 2, "Femmina",
                "Gatta molto indipendente, ama dormire al sole e osservare dalla finestra.");
        luna.setStato(StatoAnimale.ADOTTATO);
        animaleRepository.save(luna);

        Animale rocky = new Animale("Rocky", "Cane", "Pastore tedesco", 5, "Maschio",
                "Cane energico, adatto a chi ha già esperienza con cani di taglia grande.");
        rocky.setStato(StatoAnimale.IN_VALUTAZIONE);
        animaleRepository.save(rocky);

        Animale birba = new Animale("Birba", "Coniglio", "Nano", 1, "Femmina",
                "Piccola e curiosa, ha bisogno di uno spazio protetto in cui muoversi.");
        animaleRepository.save(birba);

        // Turni
        Turno turno1 = new Turno();
        turno1.setVolontario(marco);
        turno1.setAnimale(fido);
        turno1.setData(LocalDate.now().plusDays(1));
        turno1.setOraInizio(LocalTime.of(9, 0));
        turno1.setOraFine(LocalTime.of(11, 0));
        turno1.setNote("Passeggiata mattutina e controllo alimentazione");
        turnoRepository.save(turno1);

        Turno turno2 = new Turno();
        turno2.setVolontario(giulia);
        turno2.setAnimale(rocky);
        turno2.setData(LocalDate.now().plusDays(1));
        turno2.setOraInizio(LocalTime.of(15, 0));
        turno2.setOraFine(LocalTime.of(17, 0));
        turno2.setNote("Sessione di socializzazione in giardino");
        turnoRepository.save(turno2);

        Turno turno3 = new Turno();
        turno3.setVolontario(marco);
        turno3.setAnimale(birba);
        turno3.setData(LocalDate.now().plusDays(2));
        turno3.setOraInizio(LocalTime.of(10, 0));
        turno3.setOraFine(LocalTime.of(11, 0));
        turno3.setNote("Pulizia gabbia e controllo veterinario di routine");
        turnoRepository.save(turno3);

        // Richieste di adozione
        RichiestaAdozione richiestaApprovata = new RichiestaAdozione();
        richiestaApprovata.setAdottante(sara);
        richiestaApprovata.setAnimale(luna);
        richiestaApprovata.setMotivazione("Vivo da sola in appartamento e cerco una gatta tranquilla.");
        richiestaApprovata.setDataRichiesta(LocalDate.now().minusDays(10));
        richiestaApprovata.setStato(StatoRichiesta.APPROVATA);
        richiestaAdozioneRepository.save(richiestaApprovata);

        RichiestaAdozione richiestaInAttesa = new RichiestaAdozione();
        richiestaInAttesa.setAdottante(luca);
        richiestaInAttesa.setAnimale(fido);
        richiestaInAttesa.setMotivazione("Ho un giardino grande e tanta esperienza con i cani.");
        richiestaInAttesa.setDataRichiesta(LocalDate.now().minusDays(1));
        richiestaInAttesa.setStato(StatoRichiesta.IN_ATTESA);
        richiestaAdozioneRepository.save(richiestaInAttesa);

        // Recensioni
        Recensione recensione = new Recensione();
        recensione.setAnimale(luna);
        recensione.setAutore(sara);
        recensione.setTesto("Adottare Luna è stata la scelta migliore, il rifugio mi ha seguita passo passo.");
        recensione.setVoto(5);
        recensione.setData(LocalDate.now().minusDays(5));
        recensioneRepository.save(recensione);

        System.out.println(">>> Dati di prova inseriti correttamente.");
    }
}