package hello.controller;

import hello.entity.User;
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
import java.util.Map;

// Filter->构造Token->AuthenticationManager->转给Provider处理->认证处理成功后续操作或者不通过抛异常
@RestController
public class AuthController {
    private UserService userService;
    private AuthenticationManager authenticationManager;

    @Inject
    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/auth")
    @ResponseBody // 将返回值限定在body里面
    public Object auth() {
        // 这里没有接入数据库，保存的信息是在内存中的，因此暂时读取不到，返回的是anonymousUser 匿名用户
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userService.getUserByUsername(userName);

        if (loggedInUser == null) {
            return Result.success("用户没有登录!", null);
        }
        return Result.success("登录成功!", userService.getUserByUsername(userName));
    }

    @PostMapping("/auth/register")
    @ResponseBody
    public Result register(@RequestBody Map<String, String> usernameAndPassword) {
        String username = usernameAndPassword.get("username");
        String password = usernameAndPassword.get("password");
        if (username == null || password == null) {
            return Result.failure("username or password is null");
        }
        if (username.length() < 1 || username.length() > 15) {
            return Result.failure("invalid password");
        }
        if (password.length() < 1 || password.length() > 15) {
            return Result.failure("invalid password");
        }


        // 本来未考虑到并发同时注册相同用户
        // 现在使用数据库username字段改为unique则直接保存捕获异常然后抛出错误
        try {
            userService.save(username, password);
            return Result.success("注册成功!", null);

        } catch (DuplicateKeyException e) {
            e.printStackTrace();
            return Result.failure("用户已存在");
        }
    }

    @PostMapping("/auth/login")
    @ResponseBody
    public Result login(@RequestBody Map<String, String> usernameAndPassword) {
        String username = usernameAndPassword.get("username");
        String password = usernameAndPassword.get("password");

        // 命令这个服务去查找真正用户名的密码，具体实现可以是去数据库去找
        UserDetails userDetails;
        // 判断用户存在与否，如果不存在则直接返回
        try {
            userDetails = userService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            return Result.failure("用户不存在");
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
            return Result.success("登录成功", userService.getUserByUsername(username));
        } catch (BadCredentialsException e) {
            return Result.failure("密码不正确");
        }
    }

    @GetMapping("/auth/logout")
    @ResponseBody
    public Object logout() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userService.getUserByUsername(userName);
        if (loggedInUser == null) {
            return Result.success("用户没有登录!", null);
        } else {
            SecurityContextHolder.clearContext();
            return Result.success("注销成功!", null);
        }
    }

    private static class Result {
        String status;
        String msg;
        boolean isLogin;
        Object data;

        public static Result failure(String msg) {
            return new Result("fail", msg, false);
        }

        public static Result success(String msg, Object data) {
            if (data == null) {
                return new Result("ok", msg, true);
            } else {
                return new Result("ok", msg, false, data);
            }
        }

        Result(String status, String msg, boolean isLogin) {
            this(status, msg, isLogin, null);
        }

        Result(String status, String msg, boolean isLogin, Object data) {
            this.status = status;
            this.msg = msg;
            this.isLogin = isLogin;
            this.data = data;
        }

        public String getStatus() {
            return status;
        }

        public String getMsg() {
            return msg;
        }

        public boolean isLogin() {
            return isLogin;
        }

        public Object getData() {
            return data;
        }
    }

}
