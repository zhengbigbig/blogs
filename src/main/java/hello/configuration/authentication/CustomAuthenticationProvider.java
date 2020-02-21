package hello.configuration.authentication;

import hello.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;

public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Inject
    private UserService userService;
    @Inject
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * @param authentication 认证
     * @return 认证处理，返回一个Authentication的实现类则代表认证成功，返回null则代表认证失败
     * @throws AuthenticationException
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();
        if (StringUtils.isBlank(username)) {
            throw new UsernameNotFoundException("username用户名不可以为空");
        }
        if (StringUtils.isBlank(password)) {
            throw new BadCredentialsException("密码不可以为空");
        }
        //获取用户信息
        UserDetails user = userService.loadUserByUsername(username);
        //比较前端传入的密码明文和数据库中加密的密码是否相等
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            //发布密码不正确事件
            throw new BadCredentialsException("密码不正确");
        }
        //获取用户权限信息
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        return new UsernamePasswordAuthenticationToken(user, password, authorities);

    }

    /**
     * 如果该AuthenticationProvider支持传入的Authentication对象，则返回true
     *
     * @param clazz
     * @return true
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(
                UsernamePasswordAuthenticationToken.class);
    }
}