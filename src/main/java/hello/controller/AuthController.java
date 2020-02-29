package hello.controller;

import hello.entity.result.NormalResult;
import hello.entity.result.ObjectResult;
import hello.entity.result.Result;
import hello.entity.user.User;
import hello.service.impl.AuthServiceImpl;
import hello.service.impl.EmailSmsServiceImpl;
import hello.service.impl.UserServiceImpl;
import lombok.extern.java.Log;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Map;

// Filter->构造Token->AuthenticationManager->转给Provider处理->认证处理成功后续操作或者不通过抛异常
@Log
@RestController
public class AuthController {
    @Inject
    private UserServiceImpl userService;
    @Inject
    private AuthServiceImpl authService;
    @Inject
    private EmailSmsServiceImpl emailSmsService;
    @Inject
    private StringRedisTemplate redisTemplate;

    // TEST SESSION 从redis中找用户session
    @GetMapping("/auth/login")
    @ResponseBody
    public Object findByUsername(HttpSession httpSession, Principal principal) {
        // 获得详细信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal().equals("anonymousUser")) {
            return ObjectResult.failure("登录失败");
        }
        User user = (User) authentication.getPrincipal();
        return user;
    }


    @GetMapping("/auth")
    @ResponseBody // 将返回值限定在body里面
    public Object currentUser(Principal principal) {
        return authService.getCurrentUser()
                .map(user -> ObjectResult.success("获取成功", user))
                .orElse(ObjectResult.failure("当前未登录"));
    }

    @PostMapping("/auth/sendMail")
    @ResponseBody
    public Result<Object> sendMail(@RequestBody Map<String, String> registerUser) {
        return emailSmsService.sendMailIfSuccessThenSaveSms(registerUser);
    }

    @PostMapping("/auth/register")
    @ResponseBody
    public ObjectResult register(@RequestBody Map<String, String> registerUser) {
        String username = registerUser.get("username");
        String password = registerUser.get("password");
        String email = registerUser.get("email");
        String sms = registerUser.get("sms");

        ObjectResult illegalResult = authService.validateRegisterIfLegalReturnResultOrNull(username, password, email, sms);
        if (illegalResult != null) {
            return illegalResult;
        }
        // 本来未考虑到并发同时注册相同用户
        // 现在使用数据库username字段改为unique则直接保存捕获异常然后抛出错误
        try {
            emailSmsService.invalidEmailSms(email);
            userService.insert(username, password, email);
            return ObjectResult.success("注册成功!", null);

        } catch (DuplicateKeyException e) {
            e.printStackTrace();
            return ObjectResult.failure("用户已存在");
        }
    }


    @GetMapping("/auth/logout")
    @ResponseBody
    public NormalResult logout(HttpSession httpSession) {
        httpSession.invalidate();
        return NormalResult.success("注销成功！");
    }

    @PostMapping("/auth/resetPw")
    @ResponseBody
    public NormalResult resetPw(@RequestBody Map<String, String> resetParamas) {
        // email -> 查用户 然后对用户密码进行修改
        // 然后update用户密码
        String email = resetParamas.get("email");
        String password = resetParamas.get("password");
        String rePassword = resetParamas.get("rePassword");
        String sms = resetParamas.get("sms");
        if (password.equals(rePassword)) {
            return NormalResult.failure("Passwords entered twice are inconsistent");
        }
        User user = userService.getUserByUsernameOrEmail(email);
        if (null == user) {
            return NormalResult.failure("invalid email");
        }
        // 验证验证码是否一致
        if (!emailSmsService.isEqualSms(email, Integer.parseInt(sms))) {
            return NormalResult.failure("invalid sms_code");
        }
        user.setPassword(password);
        if (userService.updatePassword(user) > 0) {
            return NormalResult.success("修改成功");
        } else {
            return NormalResult.failure("修改失败");
        }
    }

    @GetMapping("/auth/test")
    @ResponseBody
    public void test() {
        ValueOperations ops = redisTemplate.opsForValue();
        ops.set("k2","v1");
        Object k1 = ops.get("k2");
        System.out.println(k1);
    }

}
