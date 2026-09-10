package it.uniroma3.siw.progettopersonale.authentication;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configurazione di Spring Security, secondo lo schema visto a lezione (slide
 * "Autenticazione e autorizzazione"): UserDetailsService basato su
 * JdbcUserDetailsManager con query dirette sulla tabella "credenziali",
 * PasswordEncoder con BCrypt, e SecurityFilterChain costruita a blocchi
 * (autorizzazione, login, logout).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final DataSource dataSource;

    public SecurityConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
        manager.setUsersByUsernameQuery(
                "SELECT username, password, 1 as enabled FROM credenziali WHERE username = ?");
        manager.setAuthoritiesByUsernameQuery(
                "SELECT username, ruolo FROM credenziali WHERE username = ?");
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.authorizeHttpRequests(authorize -> {
            authorize.requestMatchers(HttpMethod.GET,
                            "/", "/css/**", "/js/**", "/images/**", "/webjars/**",
                            "/turni", "/volontari", "/specie", "/recensioni-volontari",
                            "/register", "/login", "/error")
                    .permitAll();

            authorize.requestMatchers(HttpMethod.GET, "/animali", "/animali/{id:[0-9]+}").permitAll();

            authorize.requestMatchers(HttpMethod.POST, "/register", "/login").permitAll();

            authorize.requestMatchers("/animali/*/richiedi-adozione",
                            "/le-mie-richieste", "/le-mie-richieste/**",
                            "/animali/*/recensioni/**", "/recensioni/**")
                    .hasAuthority("ADOTTANTE");

            authorize.requestMatchers("/volontario/**", "/admin/**").hasAuthority("VOLONTARIO");

            authorize.anyRequest().authenticated();
        });

        httpSecurity.formLogin(form -> {
            form.loginPage("/login").permitAll();
            form.loginProcessingUrl("/login");
            form.usernameParameter("username");
            form.passwordParameter("password");
            form.defaultSuccessUrl("/", true);
            form.failureUrl("/login?error=true");
        });

        httpSecurity.logout(logout -> {
            logout.logoutUrl("/logout");
            logout.logoutSuccessUrl("/");
            logout.invalidateHttpSession(true);
            logout.deleteCookies("JSESSIONID");
            logout.permitAll();
        });

        return httpSecurity.build();
    }
}
