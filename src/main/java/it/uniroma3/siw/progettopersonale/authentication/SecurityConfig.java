package it.uniroma3.siw.progettopersonale.authentication;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final DataSource dataSource;
    private final CustomOidcUserService customOidcUserService;

    public SecurityConfig(DataSource dataSource, CustomOidcUserService customOidcUserService) {
        this.dataSource = dataSource;
        this.customOidcUserService = customOidcUserService;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
        manager.setUsersByUsernameQuery(
                "SELECT username, password, 1 as enabled FROM utente WHERE username = ?");
        manager.setAuthoritiesByUsernameQuery(
                "SELECT username, ruolo FROM utente WHERE username = ?");
        return manager;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.authorizeHttpRequests(authorize -> {

            // --- pagine pubbliche ---
            authorize.requestMatchers(HttpMethod.GET,
                            "/", "/css/**", "/js/**", "/images/**", "/webjars/**",
                            "/specie", "/register", "/login", "/error",
                            "/oauth2/**", "/login/oauth2/**")
                    .permitAll();

            authorize.requestMatchers(HttpMethod.GET,
                            "/animali", "/animali/{id:[0-9]+}", "/api/animali/ricerca")
                    .permitAll();

            // L'elenco delle recensioni sull'operato degli admin lo legge chiunque,
            // anche chi non ha fatto il login. Deve stare PRIMA della regola qui
            // sotto, che protegge tutto il resto sotto /recensioni.
            authorize.requestMatchers(HttpMethod.GET, "/recensioni").permitAll();

            authorize.requestMatchers(HttpMethod.POST, "/register", "/login").permitAll();

            // --- area utente normale ---
            // Scrivere, modificare ed eliminare una recensione e' riservato a chi
            // e' registrato come UTENTE: un ADMIN puo' leggerle ma non scriverne.
            authorize.requestMatchers("/recensioni", "/recensioni/**").hasAuthority("UTENTE");

            authorize.requestMatchers("/animali/*/richiedi-adozione",
                            "/le-mie-richieste", "/le-mie-richieste/**")
                    .hasAuthority("UTENTE");

            // --- area amministratore ---
            authorize.requestMatchers("/admin/**").hasAuthority("ADMIN");

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

        httpSecurity.oauth2Login(oauth2 -> {
            oauth2.loginPage("/login");
            oauth2.userInfoEndpoint(userInfo -> userInfo.oidcUserService(customOidcUserService));
            oauth2.defaultSuccessUrl("/", true);
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
