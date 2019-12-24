package hello.controller;

import hello.entity.User;
import hello.service.UserService;
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
        User loggedInUser = userService.findUserByUsername(userName);

        if (loggedInUser == null) {
            return new Result("ok", "用户没有登录", false);
        }
        return new Result("ok", "登录成功", true, userService.findUserByUsername(userName));
    }

    @PostMapping("/auth/register")
    @ResponseBody
    public Result register(@RequestBody Map<String, String> usernameAndPassword) {
        String username = usernameAndPassword.get("username");
        String password = usernameAndPassword.get("password");
        if (username == null || password == null) {
            return new Result("fail", "username or password is null", false);
        }
        if (username.length() < 1 || username.length() > 15) {
            return new Result("fail", "invalid password", false);
        }
        if (password.length() < 1 || password.length() > 15) {
            return new Result("fail", "invalid password", false);
        }

        User user = userService.findUserByUsername(username);
        // 这里是有问题的，如果多线程并发同时有俩个用户用相同的名字注册呢，会判断为空保存俩次数据库
        if (user == null) {
            userService.save(username, password);
            return new Result("ok", "成功!", false);
        } else {
            return new Result("fail", "用户已存在!", false);
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
            return new Result("fail", "用户不存在", false);
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

            return new Result("ok", "登录成功", true, userService.findUserByUsername(username));
        } catch (BadCredentialsException e) {
            return new Result("fail", "密码不正确", false);
        }
    }

    @GetMapping("/auth/logout")
    @ResponseBody
    public Object logout() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedInUser = userService.findUserByUsername(userName);
        if (loggedInUser == null) {
            return new Result("ok", "用户没有登录", false);
        } else {
            SecurityContextHolder.clearContext();
            return new Result("ok", "注销成功", false);
        }
    }

    private static class Result {
        String status;
        String msg;
        boolean isLogin;
        Object data;

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
