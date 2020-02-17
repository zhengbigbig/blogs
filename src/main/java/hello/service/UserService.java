package hello.service;

import hello.dao.MailDao;
import hello.dao.UserMapper;
import hello.entity.Mail;
import hello.entity.MailResult;
import hello.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

// 之前使用WebSecurityConfig中的UserDetailsService是Mock的，现在来实现
@Service
public class UserService implements UserDetailsService {

    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserMapper userMapper;
    private MailDao mailDao;
    private MailService mailService;

    @Inject
    public UserService(BCryptPasswordEncoder bCryptPasswordEncoder, UserMapper userMapper, MailDao mailDao, MailService mailService) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userMapper = userMapper;
        this.mailDao = mailDao;
        this.mailService = mailService;
    }

    public void save(String username, String password, String email) {
        userMapper.save(username, bCryptPasswordEncoder.encode(password), email);
    }

    public User getUserByUsername(String username) {
        return userMapper.findUserByUsernameOrEmail(username);
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

    public MailResult sendMailIfSuccessThenSaveSms(Map<String, String> registerUser) {
        // 生成6位验证码并发送邮件
        Integer code = new Random().nextInt(999999);
        try {
            mailService.sendMail(registerUser, code);
            mailDao.insertSms(new Mail(registerUser.get("email"), code));
            return MailResult.success(String.valueOf(code));
        } catch (Exception e) {
            e.printStackTrace();
            return MailResult.success("邮件发送异常");
        }

    }

    public Integer getSmsByEmail(String email) {
        return Optional.ofNullable(mailDao.getSmsByEmail(email))
                .map(mail -> mail.getSms())
                .orElse(-1);
    }

    public Mail updateSms(String email) {
        return mailDao.updateSms(email);
    }

}
