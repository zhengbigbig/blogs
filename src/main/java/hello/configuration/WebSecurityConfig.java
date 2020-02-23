package hello.configuration;

import hello.configuration.authentication.datasource.MyAccessDecisionManager;
import hello.configuration.authentication.handler.CustomAuthenticationFailHandler;
import hello.configuration.authentication.handler.CustomAuthenticationSuccessHandler;
import hello.configuration.authentication.handler.SimpleAccessDeniedHandler;
import hello.configuration.authentication.handler.SimpleAuthenticationEntryPoint;
import hello.configuration.authentication.interceptor.CustomUsernamePasswordAuthenticationFilter;
import hello.configuration.authentication.interceptor.MyFilterSecurityInterceptor;
import hello.configuration.authentication.provider.CustomAuthenticationProvider;
import hello.configuration.authentication.strategy.AjaxSessionInformationExpiredStrategy;
import hello.dao.PermissionMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import javax.inject.Inject;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
// 告诉spring 加上web安全模块，设置后，所有的请求都会被拦截
// 权限管理核心：需要实现AuthenticationManager、accessDecisionManager
// 1.登陆验证拦截器AuthenticationProcessingFilter
// 2.资源管理拦截器AbstractSecurityInterceptor
/*
 用户登录 -> AuthenticationProcessingFilter拦截 ->
 调用 AuthenticationManager -> 调用ProviderManager进行用户验证
 验证通过后，将用户权限信息封装在User放在springboot全局缓存SecurityContextHolder

 访问url -> AbstractSecurityInterceptor拦截 ->
 调用FilterInvocationSecurityMetadataSource获取被拦截url所需权限 ->
 调用权限管理器 AccessDecisionManager 通过SecurityContextHolder获取用户权限
 */
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Inject
    private UserDetailsService userService;
    @Inject
    private AjaxSessionInformationExpiredStrategy ajaxSessionInformationExpiredStrategy;
    @Inject
    private PermissionMapper permissionMapper;
    @Inject
    private CustomAuthenticationSuccessHandler successHandler;
    @Inject
    private CustomAuthenticationFailHandler failHandler;

    //    @Inject
//    private DataSource dataSource;
//
//    @Bean
//    public PersistentTokenRepository persistentTokenRepository() {
//        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
//        tokenRepository.setDataSource(dataSource);
//        // tokenRepository.setCreateTableOnStartup(true);
//        // create table persistent_logins (username varchar(64) not null, series varchar(64) primary key, token varchar(64) not null, last_used timestamp not null)
//        return tokenRepository;
//    }
//
    private final String[] ignoredURI = {
            "/index.html", "/error/**", "/static/**", // 静态资源
            "/", "/auth/**"
    };

    @Override
    public void configure(WebSecurity web) throws Exception {
        // 避免自定义过滤器交给spring，否则失效
        web.ignoring().antMatchers(ignoredURI);

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // csrf默认是开启的，会导致访问403，需要先关闭，一种跨站请求伪造，对post有效
        http
                .csrf().disable();
        http
                .authorizeRequests().anyRequest().authenticated()
                .and().exceptionHandling()
                .accessDeniedHandler(new SimpleAccessDeniedHandler())
                .authenticationEntryPoint(new SimpleAuthenticationEntryPoint())
                .and()
                .formLogin()
                .loginProcessingUrl("/login")
                .successHandler(successHandler)
                .failureHandler(failHandler)
                .permitAll()
                .and()
                .sessionManagement()
//                .invalidSessionUrl("/session/invalid")
                .maximumSessions(1) // 只能一个地方登陆
                .maxSessionsPreventsLogin(false) // 阻止其他地方登陆
                .expiredSessionStrategy(ajaxSessionInformationExpiredStrategy) // session失效后的返回
                .sessionRegistry(sessionRegistry())
                .and()
                .and()
                .addFilterBefore(customUsernamePasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(myFilterSecurityInterceptor(), FilterSecurityInterceptor.class)
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/auth/logout")
                .permitAll()
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .clearAuthentication(true);


    }

    public MyFilterSecurityInterceptor myFilterSecurityInterceptor() {
        MyFilterSecurityInterceptor myFilterSecurityInterceptor = new MyFilterSecurityInterceptor(permissionMapper);
        myFilterSecurityInterceptor.setAccessDecisionManager(new MyAccessDecisionManager());
        return myFilterSecurityInterceptor;
    }

    @Bean
    public CustomUsernamePasswordAuthenticationFilter customUsernamePasswordAuthenticationFilter() throws Exception {
        CustomUsernamePasswordAuthenticationFilter filter = new CustomUsernamePasswordAuthenticationFilter();
        ProviderManager providerManager =
                new ProviderManager(Collections.singletonList(authenticationProvider()));
        filter.setAuthenticationManager(providerManager);
        // HttpSecurity中定义总是失效，暂没找到原因
        filter.setAuthenticationSuccessHandler(successHandler);
        filter.setAuthenticationFailureHandler(failHandler);
        return filter;
    }

    /*
     * 认证管理器
     */
    @Bean
    public AuthenticationManager customAuthenticationManager() throws Exception {
        return authenticationManager();
    }

    // 全局的加密服务
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 加入自定义的安全认证
//        auth.authenticationProvider(authenticationProvider());

        auth
                .userDetailsService(userService)
                .passwordEncoder(bCryptPasswordEncoder());
    }

    // 对存储到数据库的密码进行加密
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 自定义session
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new CustomAuthenticationProvider();
    }


}