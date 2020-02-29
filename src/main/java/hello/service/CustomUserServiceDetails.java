//package hello.service;
//
//import hello.entity.user.Permission;
//import hello.entity.user.User;
//import hello.mapper.PermissionMapper;
//import hello.service.impl.UserServiceImpl;
//import hello.utils.requests.JwtUtils;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.bcrypt.BCrypt;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.PostConstruct;
//import javax.inject.Inject;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.concurrent.TimeUnit;
//
///**
// * Created by zhengzhiheng on 2020/2/29 10:24 下午
// * Description:
// */
//
//@Service
//public class CustomUserServiceDetails implements UserDetailsService {
//    private PermissionMapper permissionMapper;
//    private UserServiceImpl userService;
//    private StringRedisTemplate stringRedisTemplate;
//
//    @Inject
//    @Lazy
//    public CustomUserServiceDetails(PermissionMapper permissionMapper, UserServiceImpl userService, StringRedisTemplate stringRedisTemplate) {
//        this.permissionMapper = permissionMapper;
//        this.userService = userService;
//        this.stringRedisTemplate = stringRedisTemplate;
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        User user = userService.getUserByUsernameOrEmail(username);
//        if (user == null) {
//            throw new UsernameNotFoundException(username + "不存在！");
//        }
//        List<Permission> permissions = Optional.ofNullable(permissionMapper.findPermissionByUserId(user.getId())).orElse(new ArrayList<>());
//        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
//        // 将权限信息添加到SimpleGrantedAuthority中，之后进行全权限验证会使用该SimpleGrantedAuthority
//        for (Permission permission : permissions) {
//            authorities.add(new SimpleGrantedAuthority(permission.getName()));
//        }
//        user.setAuthorities(authorities); //用于登录时 @AuthenticationPrincipal 标签取值
//        return user;
//    }
//
//    public String getUserLoginInfoForRedis(String username) {
//        /**
//         * @todo 从数据库或者缓存中取出jwt token生成时用的salt
//         * salt = redisTemplate.opsForValue().get("token:"+username);
//         */
//        String token = stringRedisTemplate.opsForValue().get("token:" + username);
//        return token;
//    }
//
//    public String saveUserLoginToRedis(UserDetails user) {
//        // 正式开发时可以调用该方法实时生成加密的salt
//        BCrypt.gensalt();
//        /**
//         * @todo 将salt保存到数据库或者缓存中
//         * redisTemplate.opsForValue().set("token:"+username, salt, 3600, TimeUnit.SECONDS);
//         */
//        String token = JwtUtils.createToken(user, false, 3600);
//
//        stringRedisTemplate.opsForValue().set(
//                "token:" + user.getUsername(), token,
//                3600, TimeUnit.SECONDS
//
//        );
//        return token;
//    }
//
//
//    public boolean deleteUserLoginInfoToRedis(String username) {
//        Boolean delete = stringRedisTemplate.delete("token:" + username);
//        return delete;
//    }
//}
