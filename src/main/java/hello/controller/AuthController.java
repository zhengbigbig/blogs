package hello.controller;

import hello.entity.LoginResult;
import hello.service.AuthService;
import hello.service.UserService;
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
import java.util.regex.Pattern;

// Filter->构造Token->AuthenticationManager->转给Provider处理->认证处理成功后续操作或者不通过抛异常
@RestController
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final Pattern USERNAME_STANDARD = Pattern.compile("^(?!_)(?!.*?_$)[a-zA-Z0-9_\\u4e00-\\u9fa5]{2,15}$");
    private final Pattern PASSWORD_STANDARD = Pattern.compile("^[A-Za-z0-9.\\-_]{6,16}$");

    @Inject
    public AuthController(UserService userService, AuthenticationManager authenticationManager, AuthService authService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.authService = authService;
    }

    @GetMapping("/auth")
    @ResponseBody // 将返回值限定在body里面
    public Object auth() {
        // 这里没有接入数据库，保存的信息是在内存中的，因此暂时读取不到，返回的是anonymousUser 匿名用户
        return authService.getCurrentUser()
                .map(LoginResult::success)
                .orElse(LoginResult.success("用户没有登录", false));
    }

    @PostMapping("/auth/register")
    @ResponseBody
    public LoginResult register(@RequestBody Map<String, String> usernameAndPassword) {
        String username = usernameAndPassword.get("username");
        String password = usernameAndPassword.get("password");
        if (!USERNAME_STANDARD.matcher(username).find()) {
            return LoginResult.failure("invalid username");
        }
        if (!PASSWORD_STANDARD.matcher(password).find()) {
            return LoginResult.failure("invalid password");
        }
        // 本来未考虑到并发同时注册相同用户
        // 现在使用数据库username字段改为unique则直接保存捕获异常然后抛出错误
        try {
            userService.save(username, password);
            return LoginResult.success("注册成功!", null);

        } catch (DuplicateKeyException e) {
            e.printStackTrace();
            return LoginResult.failure("用户已存在");
        }
    }

    @PostMapping("/auth/login")
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
            return LoginResult.success("登录成功", userService.getUserByUsername(username));
        } catch (BadCredentialsException e) {
            return LoginResult.failure("密码不正确");
        }
    }

    @GetMapping("/auth/logout")
    @ResponseBody
    public LoginResult logout() {
        SecurityContextHolder.clearContext();
        return authService.getCurrentUser()
                .map(user -> LoginResult.success("注销成功", false))
                .orElse(LoginResult.failure("用户没有登录"));
    }


}
