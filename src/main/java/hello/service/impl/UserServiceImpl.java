package hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hello.entity.user.Permission;
import hello.entity.user.User;
import hello.mapper.PermissionMapper;
import hello.mapper.UserMapper;
import hello.service.UserService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService, UserDetailsService {
    @Inject
    private UserMapper userMapper;
    @Inject
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Inject
    private PermissionMapper permissionMapper;


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

    // D

}
