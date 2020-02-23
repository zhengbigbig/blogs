package hello.controller;

import hello.entity.result.NormalResult;
import hello.entity.result.ObjectResult;
import hello.entity.result.Result;
import hello.entity.user.User;
import hello.service.AuthService;
import hello.service.SessionService;
import hello.service.UserService;
import hello.utils.ValidateUtils;
import lombok.extern.java.Log;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

// Filter->构造Token->AuthenticationManager->转给Provider处理->认证处理成功后续操作或者不通过抛异常
@Log
@RestController
public class AuthController {
    private final UserService userService;
    private final AuthService authService;
    private final SessionService sessionService;
    private final SessionRegistry sessionRegistry;

    @Inject
    public AuthController(UserService userService, AuthService authService, SessionService sessionService, SessionRegistry sessionRegistry) {
        this.userService = userService;
        this.authService = authService;
        this.sessionService = sessionService;
        this.sessionRegistry = sessionRegistry;
    }

    @GetMapping("/auth")
    @ResponseBody //
    public ObjectResult auth(HttpServletRequest request) {
        // 这里没有接入数据库，保存的信息是在内存中的，因此暂时读取不到，返回的是anonymousUser 匿名用户
        Optional.ofNullable(sessionRegistry.getSessionInformation(request.getSession().getId()))
                .filter(sessionInformation -> !sessionInformation.isExpired())
                .map(sessionInformation -> {
                    UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) sessionInformation.getPrincipal();
                    SecurityContextHolder.getContext().setAuthentication(token);
                    return (User) token.getPrincipal();
                });
        return sessionService.loginSuccessThenSetContext(request)
                .map(user -> ObjectResult.success("获取成功", user))
                .orElse(ObjectResult.failure("别伪造session来骗人好吗"));
//        return null;
    }

    @GetMapping("/auth/currentUser")
    @ResponseBody // 将返回值限定在body里面
    public Object currentUser(Principal principal) {
        // 这里没有接入数据库，保存的信息是在内存中的，因此暂时读取不到，返回的是anonymousUser 匿名用户
        System.out.println(principal);
        return authService.getCurrentUser()
                .map(user -> ObjectResult.success("获取成功", user))
                .orElse(ObjectResult.failure("当前未登录"));
    }

    @PostMapping("/auth/sendMail")
    @ResponseBody
    public Result<Object> sendMail(@RequestBody Map<String, String> registerUser) {
        return userService.sendMailIfSuccessThenSaveSms(registerUser);
    }

    @PostMapping("/auth/register")
    @ResponseBody
    public ObjectResult register(@RequestBody Map<String, String> registerUser) {
        String username = registerUser.get("username");
        String password = registerUser.get("password");
        String email = registerUser.get("email");
        String sms = registerUser.get("sms");

        ObjectResult illegalResult = validateRegisterIfIllegal(username, password, email, sms);
        if (illegalResult != null) {
            return illegalResult;
        }

        // 本来未考虑到并发同时注册相同用户
        // 现在使用数据库username字段改为unique则直接保存捕获异常然后抛出错误
        try {
            userService.updateSms(email);
            userService.save(username, password, email);
            return ObjectResult.success("注册成功!", null);

        } catch (DuplicateKeyException e) {
            e.printStackTrace();
            return ObjectResult.failure("用户已存在");
        }
    }

    private ObjectResult validateRegisterIfIllegal(String username, String password, String email, String sms) {
        if (userService.isUserExist(username)) {
            return ObjectResult.failure("exist username");
        }

        if (userService.isUserExist(email)) {
            return ObjectResult.failure("exist email");
        }

        if (!ValidateUtils.username(username)) {
            return ObjectResult.failure("invalid username");
        }
        if (!ValidateUtils.password(password)) {
            return ObjectResult.failure("invalid password");
        }

        if (!userService.isEqualSms(email, Integer.parseInt(sms))) {
            return ObjectResult.failure("invalid sms");
        }
        return null;
    }

    @GetMapping("/auth/logout")
    @ResponseBody
    public ObjectResult logout() {
//        SecurityContextHolder.clearContext();
        return authService.getCurrentUser()
                .map(user -> ObjectResult.success("注销成功", false))
                .orElse(ObjectResult.failure("未登录！"));
    }

    @PostMapping("/auth/resetPw")
    @ResponseBody
    public NormalResult resetPw(@RequestBody Map<String, String> resetParamas) {
        // email -> 查用户 然后对用户密码进行修改
        // 然后update用户密码
        String email = resetParamas.get("email");
        String password = resetParamas.get("password");
        String sms = resetParamas.get("sms");

        Optional<User> user = Optional.ofNullable(
                userService.getUserByUsernameOrEmail(email));
        if (!user.isPresent()) {
            return NormalResult.failure("invalid email");
        }
        // 验证验证码是否一致
        if (!userService.isEqualSms(email, Integer.parseInt(sms))) {
            return NormalResult.failure("invalid sms_code");
        }
        if (userService.updateUserPassword(user.get(), password) > 0) {
            return NormalResult.success("修改成功");
        } else {
            return NormalResult.failure("修改失败");
        }
    }


}
