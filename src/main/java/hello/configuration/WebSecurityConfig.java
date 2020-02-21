package hello.configuration;

import hello.configuration.authentication.CustomAuthenticationProvider;
import hello.configuration.interceptor.MyAccessDecisionManager;
import hello.configuration.interceptor.MyFilterSecurityInterceptor;
import hello.configuration.interceptor.MyInvocationSecurityMetadataSourceService;
import hello.configuration.session.AjaxSessionInformationExpiredStrategy;
import hello.configuration.session.CustomUsernamePasswordAuthenticationFilter;
import hello.configuration.session.MyValidCodeProcessingFilter;
import hello.configuration.unauthenticate.SimpleAccessDeniedHandler;
import hello.configuration.unauthenticate.SimpleAuthenticationEntryPoint;
import hello.dao.PermissionMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
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
//            "/auth/**",
    };

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(ignoredURI);

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // csrf默认是开启的，会导致访问403，需要先关闭，一种跨站请求伪造，对post有效
        http
                .csrf().disable().cors();
        http
                // 授权请求，通配符匹配路径，允许匹配的所有
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                .accessDeniedHandler(new SimpleAccessDeniedHandler()).authenticationEntryPoint(new SimpleAuthenticationEntryPoint());
        http
                .sessionManagement()
                .maximumSessions(1) // 只能一个地方登陆
                .maxSessionsPreventsLogin(false) // 阻止其他地方登陆
                .expiredSessionStrategy(ajaxSessionInformationExpiredStrategy) // session失效后的返回
                .sessionRegistry(sessionRegistry());
        http
                .logout()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll();
        // 过滤器

        http
                .addFilterBefore(new CustomUsernamePasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(myFilterSecurityInterceptor(), FilterSecurityInterceptor.class);


    }

    public MyFilterSecurityInterceptor myFilterSecurityInterceptor() {
        MyFilterSecurityInterceptor myFilterSecurityInterceptor = new MyFilterSecurityInterceptor(permissionMapper);
        myFilterSecurityInterceptor.setAccessDecisionManager(new MyAccessDecisionManager());
        return myFilterSecurityInterceptor;
    }

    /*
     * 认证管理器
     */
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // 全局的加密服务
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 加入自定义的安全认证
        auth
                .userDetailsService(userService)
                .passwordEncoder(bCryptPasswordEncoder())
                .and()
                .authenticationProvider(authenticationProvider());
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
        AuthenticationProvider authenticationProvider = new CustomAuthenticationProvider();
        return authenticationProvider;
    }
}