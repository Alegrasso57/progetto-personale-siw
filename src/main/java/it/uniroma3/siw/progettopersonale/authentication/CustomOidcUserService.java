package it.uniroma3.siw.progettopersonale.authentication;

import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import it.uniroma3.siw.progettopersonale.model.Utente;
import it.uniroma3.siw.progettopersonale.service.UtenteService;

/**
 * Al primo accesso con Google crea (o ritrova) l'Utente corrispondente
 * all'email Google, cosi' chi entra con Google ha un profilo nel database
 * come chi si registra con la form classica. Il ruolo assegnato e' sempre
 * UTENTE: l'amministratore e' creato all'avvio da DataInitializer.
 */
@Service
public class CustomOidcUserService extends OidcUserService {

    private final UtenteService utenteService;

    public CustomOidcUserService(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();
        String nome = oidcUser.getGivenName();
        String cognome = oidcUser.getFamilyName();

        Utente utente = utenteService.trovaOCreaPerOAuth2(email, nome, cognome);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(utente.getRuolo().name()));

        return new CustomOidcUser(oidcUser, utente.getUsername(), authorities);
    }
}
