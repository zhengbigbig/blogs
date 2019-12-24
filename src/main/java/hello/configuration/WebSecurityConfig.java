package hello.configuration;

import hello.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.inject.Inject;

@Configuration
@EnableWebSecurity
// 告诉spring 加上web安全模块，设置后，所有的请求都会被拦截
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Inject
    private UserService userService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable(); // 默认是开启的，会导致访问403，需要先关闭，一种跨站请求伪造，对post有效
        http
                // 授权请求，通配符匹配路径，允许匹配的所有
                .authorizeRequests()
                .antMatchers("/", "/auth/**").permitAll()
                .antMatchers("/xxx").authenticated()
                .anyRequest().permitAll();
    }

    // 鉴权
    @Bean
    public AuthenticationManager customAuthenticationManager() throws Exception {
        return authenticationManager();
    }

    // 全局的加密服务
    @Inject
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder());
    }

    // 对存储到数据库的密码进行加密
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}