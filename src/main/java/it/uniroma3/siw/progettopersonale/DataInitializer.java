package it.uniroma3.siw.progettopersonale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import it.uniroma3.siw.progettopersonale.model.Ruolo;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.repository.UtenteRepository;

/**
 * All'avvio garantisce che esista almeno un ADMIN: senza, nessuno potrebbe
 * inserire animali, dichiarare turni o approvare adozioni, e non si potrebbe
 * entrare nell'area /admin. L'amministratore non si registra dal sito: viene
 * creato qui.
 *
 * Se un admin esiste gia' non fa nulla, quindi e' sicuro a ogni riavvio e non
 * tocca mai i dati reali.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        if (!utenteRepository.findByRuolo(Ruolo.ADMIN).isEmpty()) {
            return;
        }

        Utente admin = new Utente();
        admin.setNome("Admin");
        admin.setCognome("Rifugio");
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setRuolo(Ruolo.ADMIN);
        utenteRepository.save(admin);

        logger.info(">>> Nessun amministratore presente: creato l'account iniziale "
                + "username 'admin' / password 'admin'. Cambia la password dopo il primo accesso.");
    }
}
