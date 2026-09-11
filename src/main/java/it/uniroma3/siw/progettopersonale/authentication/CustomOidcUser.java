package it.uniroma3.siw.progettopersonale.authentication;

import java.util.Collection;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Adatta il profilo Google (OidcUser) al contratto UserDetails usato in tutto
 * il resto dell'app (vedi SecurityConfig, che si aspetta un UserDetailsService
 * basato su JdbcUserDetailsManager): cosi' il codice esistente che fa
 * "(UserDetails) SecurityContextHolder...getPrincipal()" e poi
 * utenteService.findByUsername(userDetails.getUsername()) funziona anche per
 * chi accede con Google, usando come "username" lo stesso valore salvato
 * nella riga Credenziali creata/trovata per l'email Google (vedi
 * CredenzialiService.trovaOCreaPerOAuth2 e CustomOidcUserService).
 */
public class CustomOidcUser implements OidcUser, UserDetails {

    private final OidcUser oidcUser;
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomOidcUser(OidcUser oidcUser, String username, Collection<? extends GrantedAuthority> authorities) {
        this.oidcUser = oidcUser;
        this.username = username;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Map<String, Object> getClaims() {
        return oidcUser.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUser.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcUser.getIdToken();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oidcUser.getAttributes();
    }

    @Override
    public String getName() {
        return oidcUser.getName();
    }
}
