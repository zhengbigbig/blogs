package hello.controller;

import hello.entity.result.LoginResult;
import hello.entity.result.NormalResult;
import hello.entity.result.Result;
import hello.entity.user.User;
import hello.service.AuthService;
import hello.service.UserService;
import hello.utils.ValidateUtils;
import lombok.extern.java.Log;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

// Filter->构造Token->AuthenticationManager->转给Provider处理->认证处理成功后续操作或者不通过抛异常
@Log
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final AuthService authService;

    @Inject
    public AuthController(UserService userService, AuthenticationManager authenticationManager, AuthService authService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.authService = authService;
    }

    @GetMapping("/currentUser")
    @ResponseBody // 将返回值限定在body里面
    public Object auth() {
        // 这里没有接入数据库，保存的信息是在内存中的，因此暂时读取不到，返回的是anonymousUser 匿名用户
        return authService.getCurrentUser()
                .map(LoginResult::success)
                .orElse(LoginResult.failure("当前未登录"));
    }

    @PostMapping("/sendMail")
    @ResponseBody
    public Result<Object> sendMail(@RequestBody Map<String, String> registerUser) {
        return userService.sendMailIfSuccessThenSaveSms(registerUser);
    }

    @PostMapping("/register")
    @ResponseBody
    public LoginResult register(@RequestBody Map<String, String> registerUser) {
        String username = registerUser.get("username");
        String password = registerUser.get("password");
        String email = registerUser.get("email");
        String sms = registerUser.get("sms");

        LoginResult illegalResult = validateRegisterIfIllegal(username, password, email, sms);
        if (illegalResult != null) {
            return illegalResult;
        }

        // 本来未考虑到并发同时注册相同用户
        // 现在使用数据库username字段改为unique则直接保存捕获异常然后抛出错误
        try {
            userService.updateSms(email);
            userService.save(username, password, email);
            return LoginResult.success("注册成功!", null);

        } catch (DuplicateKeyException e) {
            e.printStackTrace();
            return LoginResult.failure("用户已存在");
        }
    }

    private LoginResult validateRegisterIfIllegal(String username, String password, String email, String sms) {
        if (userService.isUserExist(username)) {
            return LoginResult.failure("exist username");
        }

        if (userService.isUserExist(email)) {
            return LoginResult.failure("exist email");
        }

        if (!ValidateUtils.username(username)) {
            return LoginResult.failure("invalid username");
        }
        if (!ValidateUtils.password(password)) {
            return LoginResult.failure("invalid password");
        }

        if (!userService.isEqualSms(email, Integer.parseInt(sms))) {
            return LoginResult.failure("invalid sms");
        }
        return null;
    }

    @PostMapping("/login")
    @ResponseBody
    public Object login(@RequestBody Map<String, String> usernameAndPassword, HttpServletRequest request) {
//        if (request.getHeader("user-agent") == null || !request.getHeader("user-agent").contains("Mozilla")) {
//            return "死爬虫去死吧";
//        }

        String username = usernameAndPassword.get("username");
        String password = usernameAndPassword.get("password");

        // 命令这个服务去查找真正用户名的密码，具体实现可以是去数据库去找
        UserDetails userDetails;
        // 判断用户存在与否，如果不存在则直接返回
        try {
            userDetails = userService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            return LoginResult.failure("用户不存在");
        }

        // 先构建一个未认证的token，token只是一个载体而已
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
        // 鉴权
        try {
            //  让真正的密码和请求的密码进行比对
            // AuthenticationManager 还可以提供多种认真方式，譬如密码，验证码，自定义AuthenticationProvider
            // 怎么确定使用哪个provider？ 只需要根据Token的类型，传入token，交给AuthenticationManager provider处理
            authenticationManager.authenticate(token);
            // 如果密码对比正确，则设置这个token
            // 把用户信息保存在内存中某一个地方
            // Cookie
            SecurityContextHolder.getContext().setAuthentication(token);
            return LoginResult.success("登录成功", userService.getUserByUsernameOrEmail(username));
        } catch (BadCredentialsException e) {
            return LoginResult.failure("密码不正确");
        }
    }

    @GetMapping("/logout")
    @ResponseBody
    public LoginResult logout() {
        SecurityContextHolder.clearContext();
        return authService.getCurrentUser()
                .map(user -> LoginResult.success("注销成功", false))
                .orElse(LoginResult.failure("未登录！"));
    }

    @PostMapping("/resetPw")
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
