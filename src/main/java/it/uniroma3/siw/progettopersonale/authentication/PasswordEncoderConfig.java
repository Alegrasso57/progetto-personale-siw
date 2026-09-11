package it.uniroma3.siw.progettopersonale.authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Bean del PasswordEncoder isolato in una classe di configurazione a se',
 * senza altre dipendenze nel costruttore: se restasse dentro SecurityConfig
 * (che ora dipende da CustomOidcUserService, che a sua volta dipende da
 * CredenzialiService, che nel costruttore vuole un PasswordEncoder) si
 * creerebbe un ciclo, perche' per costruire il bean PasswordEncoder Spring
 * dovrebbe prima istanziare SecurityConfig. Spostandolo qui il ciclo si
 * rompe: questa classe non ha bisogno di nessun altro bean.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
