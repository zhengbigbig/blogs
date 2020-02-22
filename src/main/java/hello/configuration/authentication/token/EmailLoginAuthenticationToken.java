package hello.configuration.authentication.token;

import lombok.extern.java.Log;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Log
public class EmailLoginAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private static final long serialVersionUID = -6785581280958884706L;

    private Object session;

    public EmailLoginAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
        super.setAuthenticated(false);
    }

    public EmailLoginAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
        // must use super, as we override
        super.setAuthenticated(true);
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        if (authenticated) {
            throw new IllegalArgumentException(
                    "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }
        super.setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return super.getCredentials();
    }

    @Override
    public Object getPrincipal() {
        return super.getPrincipal();
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
    }

    public Object getSession() {
        return session;
    }

    public void setSession(Object session) {
        this.session = session;
    }
}
