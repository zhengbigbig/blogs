package hello.service;

import hello.entity.LoginResult;
import hello.entity.MailResult;
import hello.entity.User;
import hello.dao.UserMapper;
import hello.utils.MailUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

// 之前使用WebSecurityConfig中的UserDetailsService是Mock的，现在来实现
@Service
public class UserService implements UserDetailsService {

    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserMapper userMapper;

    @Inject
    public UserService(BCryptPasswordEncoder bCryptPasswordEncoder, UserMapper userMapper) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userMapper = userMapper;
    }

    public void save(String username, String password) {
        userMapper.save(username, bCryptPasswordEncoder.encode(password));
    }

    public User getUserByUsername(String username) {
        return userMapper.findUserByUsername(username);
    }

    // 自定义UserDetailsService
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (getUserByUsername(username) == null) {
            throw new UsernameNotFoundException(username + "不存在！");
        }
        User user = getUserByUsername(username);

        return new org.springframework.security.core.userdetails.User(username, user.getEncryptedPassword(), Collections.emptyList());
    }

    // 邮箱发送
    public MailResult sendMail(Map<String,String> registerUser) {
        // 生成6位验证码并发送邮件
        Integer code = new Random().nextInt(999999);
        try {
            MailUtils.sendMail(registerUser, code);
            return MailResult.success(String.valueOf(code));
        } catch (Exception e) {
            e.printStackTrace();
            return MailResult.success("邮件发送异常");
        }

    }

}
