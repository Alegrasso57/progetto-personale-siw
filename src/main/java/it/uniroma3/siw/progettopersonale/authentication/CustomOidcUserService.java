package it.uniroma3.siw.progettopersonale.authentication;

import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import it.uniroma3.siw.progettopersonale.model.Credenziali;
import it.uniroma3.siw.progettopersonale.service.CredenzialiService;

/**
 * Al primo accesso con Google, crea (o ritrova) la riga Credenziali/Utente
 * corrispondente all'email Google (vedi CredenzialiService.trovaOCreaPerOAuth2),
 * cosi' che chi accede con Google abbia comunque un profilo Utente nel
 * database, come chi si registra con il form classico. Il ruolo assegnato a
 * un nuovo accesso Google e' sempre ADOTTANTE: diventare volontario richiede
 * comunque la registrazione classica con il codice del centro (vedi
 * CredenzialiService.CODICE_VOLONTARIO).
 */
@Service
public class CustomOidcUserService extends OidcUserService {

    private final CredenzialiService credenzialiService;

    public CustomOidcUserService(CredenzialiService credenzialiService) {
        this.credenzialiService = credenzialiService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();
        String nome = oidcUser.getGivenName();
        String cognome = oidcUser.getFamilyName();

        Credenziali credenziali = credenzialiService.trovaOCreaPerOAuth2(email, nome, cognome);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(credenziali.getRuolo().name()));

        return new CustomOidcUser(oidcUser, credenziali.getUsername(), authorities);
    }
}
