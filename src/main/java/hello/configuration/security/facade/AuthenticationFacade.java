package hello.configuration.security.facade;

import lombok.extern.java.Log;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Log
public class AuthenticationFacade implements IAuthenticationFacade {
    @Override
    public Authentication getAuthentication() {
        log.info("AuthenticationFacade: --------  " + Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication()).map(Authentication::getPrincipal).orElse("anons"));
        return SecurityContextHolder.getContext().getAuthentication();
    }
}