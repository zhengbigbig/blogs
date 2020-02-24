package hello.service;

import hello.configuration.security.facade.AuthenticationFacade;
import hello.entity.user.User;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Optional;

@Service
public class AuthService {
    private final UserService userService;
    private final AuthenticationFacade authenticationFacade;

    @Inject
    public AuthService(UserService userService, AuthenticationFacade authenticationFacade) {
        this.userService = userService;
        this.authenticationFacade = authenticationFacade;
    }

    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(authenticationFacade.getAuthentication().getPrincipal())
                .map(authentication ->
                        userService.getUserByUsernameOrEmail(authentication instanceof User ? ((User) authentication).getUsername():null)
                );
    }
}
