package hello.service;

import com.google.common.collect.ImmutableMap;
import hello.dao.MailDao;
import hello.dao.PermissionMapper;
import hello.dao.UserDao;
import hello.entity.Mail;
import hello.entity.result.MailResult;
import hello.entity.result.NormalResult;
import hello.entity.result.Result;
import hello.entity.user.Permission;
import hello.entity.user.User;
import lombok.extern.java.Log;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

// 之前使用WebSecurityConfig中的UserDetailsService是Mock的，现在来实现
@Log
@Service
public class UserService implements UserDetailsService {

    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserDao userDao;
    private MailDao mailDao;
    private MailService mailService;
    private PermissionMapper permissionMapper;

    @Inject
    private SessionRegistry sessionRegistry;

    @Inject
    public UserService(BCryptPasswordEncoder bCryptPasswordEncoder, UserDao userDao, MailDao mailDao, MailService mailService, PermissionMapper permissionMapper) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userDao = userDao;
        this.mailDao = mailDao;
        this.mailService = mailService;
        this.permissionMapper = permissionMapper;
    }

    // 自定义UserDetailsService
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUserByUsernameOrEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException(username + "不存在！");
        }
        List<Permission> permissions = Optional.ofNullable(permissionMapper.findPermissionByUserId(user.getId())).orElse(new ArrayList<>());
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        // 将权限信息添加到SimpleGrantedAuthority中，之后进行全权限验证会使用该SimpleGrantedAuthority
        for (Permission permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission.getName()));
        }
        user.setAuthorities(authorities); //用于登录时 @AuthenticationPrincipal 标签取值
        return user;
    }

    public void save(String username, String password, String email) {
        ImmutableMap<String, String> user = ImmutableMap.of(
                "username", username, "password", bCryptPasswordEncoder.encode(password), "email", email
        );
        userDao.save(user);
    }

    public User getUserByUsernameOrEmail(String username) {
        return userDao.findUserByUsernameOrEmail(username);
    }

    public int updateUser(User user) {
        return userDao.updateUser(user);
    }

    public int updateUserPassword(User user, String password) {
        user.setEncryptedPassword(bCryptPasswordEncoder.encode(password));
        return userDao.updateUser(user);
    }

    public boolean isUserExist(String searchName) {
        return Optional.ofNullable(getUserByUsernameOrEmail(searchName))
                .map(user -> true).orElse(false);
    }

    // 生成6位验证码并发送邮件，发送后存入数据库
    public Result<Object> sendMailIfSuccessThenSaveSms(Map<String, String> registerUser) {
        String email = registerUser.get("email");
        Integer code = new Random().nextInt(999999);
        try {
            mailService.sendMail(registerUser, code);
            mailDao.insertSms(new Mail(email, code));
            return MailResult.success("获取成功", new Mail(email, code));
        } catch (Exception e) {
            e.printStackTrace();
            return NormalResult.failure("邮件发送异常");
        }

    }

    public Integer getSmsByEmail(String email) {
        return Optional.ofNullable(mailDao.getSmsByEmail(email))
                .map(mail -> mail.getSms())
                .orElse(-1);
    }

    public boolean isEqualSms(String email, Integer fromRequest) {
        Integer sms = getSmsByEmail(email);
        return sms != -1 && sms.equals(fromRequest);
    }

    public int updateSms(String email) {
        return mailDao.updateSms(email);
    }

}
