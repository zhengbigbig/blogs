package hello.service;

import hello.entity.user.User;
import hello.service.impl.UserServiceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Optional;

@Service
public class AuthService {
    private final UserServiceImpl userService;

    @Inject
    public AuthService(UserServiceImpl userService) {
        this.userService = userService;
    }

    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Optional.ofNullable(authentication == null ? null : authentication.getPrincipal())
                .map(auth ->
                        userService.getUserByUsernameOrEmail(auth instanceof User ? ((User) auth).getUsername() : null)
                );
    }
}
