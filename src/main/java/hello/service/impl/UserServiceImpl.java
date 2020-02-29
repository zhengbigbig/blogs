package hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hello.entity.user.Permission;
import hello.entity.user.User;
import hello.mapper.PermissionMapper;
import hello.mapper.UserMapper;
import hello.service.UserService;
import hello.utils.requests.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService, UserDetailsService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private PermissionMapper permissionMapper;
    private StringRedisTemplate stringRedisTemplate;


    public String getUserLoginInfoForRedis(String username) {
        /**
         * @todo 从数据库或者缓存中取出jwt token生成时用的salt
         * salt = redisTemplate.opsForValue().get("token:"+username);
         */
        String token = stringRedisTemplate.opsForValue().get("token:" + username);
        return token;
    }

    public String saveUserLoginToRedis(UserDetails user) {
        // 正式开发时可以调用该方法实时生成加密的salt
        BCrypt.gensalt();
        /**
         * @todo 将salt保存到数据库或者缓存中
         * redisTemplate.opsForValue().set("token:"+username, salt, 3600, TimeUnit.SECONDS);
         */
        String token = JwtUtils.createToken(user, false, 3600);

        stringRedisTemplate.opsForValue().set(
                "token:" + user.getUsername(), token,
                3600, TimeUnit.SECONDS

        );
        return token;
    }

    public boolean deleteUserLoginInfoToRedis(String username) {
        Boolean delete = stringRedisTemplate.delete("token:" + username);
        return delete;
    }


    // C
    public int insert(String username, String password, String email) {
        User user = User.create(null, username, bCryptPasswordEncoder.encode(password), email);
        return userMapper.insert(user);
    }

    // R
    public User getUserByUsernameOrEmail(String username) {
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("state", 1)
                .and(
                        i -> i.eq("username", username)
                                .or().eq("email", username)
                );
        User user = getOne(query,false);
        return user;
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
