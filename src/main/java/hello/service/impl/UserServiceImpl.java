package hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hello.dao.MailDao;
import hello.entity.Mail;
import hello.entity.result.MailResult;
import hello.entity.result.NormalResult;
import hello.entity.result.Result;
import hello.entity.user.Permission;
import hello.entity.user.User;
import hello.mapper.PermissionMapper;
import hello.mapper.UserMapper;
import hello.service.MailService;
import hello.service.UserService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService, UserDetailsService {

    private UserMapper userMapper;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private MailDao mailDao;
    private MailService mailService;
    private PermissionMapper permissionMapper;

    @Inject
    public UserServiceImpl(UserMapper userMapper, BCryptPasswordEncoder bCryptPasswordEncoder, MailDao mailDao, MailService mailService, PermissionMapper permissionMapper) {
        this.userMapper = userMapper;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.mailDao = mailDao;
        this.mailService = mailService;
        this.permissionMapper = permissionMapper;
    }

    // C
    public int insert(String username, String password, String email) {
        User user = User.create(null, username, bCryptPasswordEncoder.encode(password), email);
        return userMapper.insert(user);
    }

    // R
    public User getUserByUsernameOrEmail(String username) {
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("state", 1).and(
                i -> i.eq("username", username)
                        .or().eq("email", username)
        );
        return userMapper.getUserByUsernameOrEmail(query);
    }

    // U
    public int updateUser(User user) {
        return userMapper.updateUser(user);
    }

    public int updatePassword(User user) {
        user.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return userMapper.updateById(user);
    }

    // R
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
    // D

}
