package hello.service;

import hello.entity.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {
    private final UserService userService;
    private final SessionRegistry sessionRegistry;

    @Inject
    public AuthService(UserService userService, SessionRegistry sessionRegistry) {
        this.userService = userService;
        this.sessionRegistry = sessionRegistry;
    }

    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Optional.ofNullable(userService.getUserByUsernameOrEmail(authentication == null ? null : authentication.getName()));
    }

    private void invalidateSession(String username) {
        List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
        for (int j = 0; j < allPrincipals.size(); j++) {
            User customUser = (User) allPrincipals.get(j);
            if (customUser.getUsername().equals(username)) {
                List<SessionInformation> allSessions = sessionRegistry.getAllSessions(customUser, false);
                if (allSessions != null) {
                    for (int i = 0; i < allSessions.size(); i++) {
                        SessionInformation sessionInformation = allSessions.get(i);
                        sessionInformation.expireNow();
                        sessionRegistry.removeSessionInformation(sessionInformation.getSessionId());
                    }
                }
            }
        }
    }
}
