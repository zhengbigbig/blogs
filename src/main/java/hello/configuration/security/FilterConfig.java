package hello.configuration.security;

import hello.configuration.security.filter.JwtAuthenticationFilter;
import hello.configuration.security.filter.JwtLoginFilter;
import hello.configuration.security.handler.CustomLoginFailureHandler;
import hello.configuration.security.handler.CustomLoginSuccessHandler;
import hello.configuration.security.handler.JwtAuthenticationSuccessHandler;
import hello.service.impl.UserServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;

import javax.inject.Inject;

/**
 * Created by zhengzhiheng on 2020/3/1 4:09 下午
 * Description: 登录登出鉴权相关配置
 */

@Configuration
public class FilterConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    @Inject
    private UserServiceImpl userService;

    @Override
    public void configure(HttpSecurity builder) throws Exception {
        builder
                .addFilterBefore(jwtAuthenticationFilter(), LogoutFilter.class)
                .addFilterAfter(jwtLoginFilter(), LogoutFilter.class);
    }

    // 该过滤器作用是过滤所有的请求，然后看有没有携带token，若携带token则把用户信息存到上下文，若没有则清空上下文，继续下面的filter
    // 若请求是需要权限的请求，后面FilterSecurityInterceptor会进一步进行处理
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        // 使用自带的manager
        filter.setAuthenticationSuccessHandler(jwtRefreshSuccessHandler());
        filter.setAuthenticationFailureHandler(new CustomLoginFailureHandler());
        return filter;
    }

    // 登录拦截，验证通过则新建token
    @Bean
    public JwtLoginFilter jwtLoginFilter() throws Exception {
        JwtLoginFilter filter = new JwtLoginFilter();
        filter.setAuthenticationFailureHandler(new CustomLoginFailureHandler());
        filter.setSessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy());
        filter.setAuthenticationSuccessHandler(new CustomLoginSuccessHandler(userService));
        return filter;
    }

    @Bean
    protected CustomLoginSuccessHandler jsonLoginSuccessHandler() {
        return new CustomLoginSuccessHandler(userService);
    }

    @Bean
    protected JwtAuthenticationSuccessHandler jwtRefreshSuccessHandler() {
        return new JwtAuthenticationSuccessHandler();
    }
}
