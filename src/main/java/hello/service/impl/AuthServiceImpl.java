package hello.service.impl;

import hello.entity.result.NormalResult;
import hello.entity.result.ObjectResult;
import hello.entity.user.User;
import hello.utils.ValidateUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Optional;

@Service
public class AuthServiceImpl {
    @Inject
    private UserServiceImpl userService;
    @Inject
    private EmailSmsServiceImpl emailSmsService;


    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Optional.ofNullable(authentication == null ? null : authentication.getPrincipal())
                .map(auth ->
                        userService.getUserByUsernameOrEmail(auth instanceof User ? ((User) auth).getUsername() : null)
                );
    }

    public boolean isUserExist(String searchName) {
        return Optional.ofNullable(userService.getUserByUsernameOrEmail(searchName)).isPresent();
    }

    public ObjectResult validateRegisterIfLegalReturnResultOrNull(String username, String password, String email, String sms) {
        if (isUserExist(username)) {
            return ObjectResult.failure("exist username");
        }
        if (isUserExist(email)) {
            return ObjectResult.failure("exist email");
        }

        if (!ValidateUtils.username(username)) {
            return ObjectResult.failure("invalid username");
        }
        if (!ValidateUtils.password(password)) {
            return ObjectResult.failure("invalid password");
        }
        if (!emailSmsService.isEqualSms(email, Integer.parseInt(sms))) {
            return ObjectResult.failure("invalid sms");
        }
        return null;
    }

    public NormalResult validateResetPwIfLegalReturnResultOrNull(String email) {
        return null;
    }

}
