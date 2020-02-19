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

    private void invalidateSession(User user){
        List<Object> o= sessionRegistry.getAllPrincipals();
        for (Object principal : o) {
            if (principal instanceof User) {
                final User loggedUser = (User) principal;
                if (user.getUsername().equals(loggedUser.getUsername())) {
                    List<SessionInformation> sessionsInfo = sessionRegistry.getAllSessions(principal, false);
                    if (null != sessionsInfo && sessionsInfo.size() > 0) {
                        for (SessionInformation sessionInformation : sessionsInfo) {
                            sessionInformation.expireNow();
                        }
                    }
                }
            }
        }
    }
}
