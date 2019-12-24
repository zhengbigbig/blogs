package hello.service;

import hello.entity.User;
import hello.dao.UserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collections;

// 之前使用WebSecurityConfig中的UserDetailsService是Mock的，现在来实现
@Service
public class UserService implements UserDetailsService {

    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserMapper userMapper;

    @Inject
    public UserService(BCryptPasswordEncoder bCryptPasswordEncoder, UserMapper userMapper) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userMapper = userMapper;
        save("abc", "abc");
    }

    public void save(String username, String password) {
        userMapper.save(username, bCryptPasswordEncoder.encode(password));
    }

    public User findUserByUsername(String username) {
        return userMapper.findUserByUsername(username);
    }

    // 自定义UserDetailsService
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (findUserByUsername(username) == null) {
            throw new UsernameNotFoundException(username + "不存在！");
        }
        User user = findUserByUsername(username);

        return new org.springframework.security.core.userdetails.User(username, user.getEncryptedPassword(), Collections.emptyList());
    }
}
