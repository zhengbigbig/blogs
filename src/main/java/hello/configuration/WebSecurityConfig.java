package hello.configuration;

import hello.configuration.security.datasource.MyAccessDecisionManager;
import hello.configuration.security.datasource.MyInvocationSecurityMetadataSourceService;
import hello.configuration.security.handler.*;
import hello.configuration.security.interceptor.CustomUsernamePasswordAuthenticationFilter;
import hello.configuration.security.provider.CustomEmailAuthenticationProvider;
import hello.configuration.security.strategy.CustomExpiredSessionStrategy;
import hello.dao.PermissionMapper;
import lombok.extern.java.Log;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collections;

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
@Log
@Configuration
@EnableWebSecurity
public class WebSecurityConfig<S extends Session> extends WebSecurityConfigurerAdapter {
    @Inject
    private PermissionMapper permissionMapper;
    @Inject
    private CustomEmailAuthenticationProvider customEmailAuthenticationProvider;
    private final String[] ignoredWebURI = {
            "/", "/index.html", "/error/**", "/static/**", // 静态资源
            "/js/**", "/css/**", "/fonts/**"
    };

    private final String[] securityUrlPermit = {
            "/auth/**", "/favicon.ico"
    };

    @Inject
    private FindByIndexNameSessionRepository<S> sessionRepository;

    // 避免自定义过滤器交给spring，否则失效
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(ignoredWebURI);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // csrf默认是开启的，会导致访问403，需要先关闭，一种跨站请求伪造，对post有效
        http
                .csrf().disable().cors()
                .and()
                .addFilterBefore(customUsernamePasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests().antMatchers(securityUrlPermit).permitAll();
        http
                .logout().logoutUrl(ConstantConfig.WEB_URL.LOGOUT.getUrl())
                .addLogoutHandler(new CustomLogoutHandler())
                .permitAll()
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                // filter security
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O postProcess(O fsi) {
                        fsi.setSecurityMetadataSource(mySecurityMetadataSource());
                        fsi.setAccessDecisionManager(myAccessDecisionManager());
                        return fsi;
                    }
                })
                .and().authorizeRequests().anyRequest().authenticated() //对其他接口的权限限制为登录后才能访问
                .and()
                // 401 403 处理
                .exceptionHandling()
                .accessDeniedHandler(new SimpleAccessDeniedHandler())
                .authenticationEntryPoint(new SimpleAuthenticationEntryPoint())
                // session
                .and().sessionManagement()
                // 设置NEVER避免spring security创建新的session 导致maximumSessions无效
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                .invalidSessionStrategy(new CustomInvalidSessionStrategy()) // session无效时处理策略，暂不用
//                .invalidSessionUrl("/session/invalid") // session无效跳转
                .maximumSessions(1) // 只能一个地方登陆
                .maxSessionsPreventsLogin(false) // 阻止其他地方登陆
                .expiredSessionStrategy(new CustomExpiredSessionStrategy()) /* session失效后的返回*/
                .sessionRegistry(sessionRegistry());

    }

    /**
     * 资源管理,决策放行
     *
     * @return FilterInvocationSecurityMetadataSource
     */
    @Bean
    public FilterInvocationSecurityMetadataSource mySecurityMetadataSource() {
        return new MyInvocationSecurityMetadataSourceService(permissionMapper);
    }

    @Bean
    public AccessDecisionManager myAccessDecisionManager() {
        return new MyAccessDecisionManager();
    }

    // 注册过滤器配置
    @Bean
    public CustomUsernamePasswordAuthenticationFilter customUsernamePasswordAuthenticationFilter() throws Exception {
        CustomUsernamePasswordAuthenticationFilter filter = new CustomUsernamePasswordAuthenticationFilter();
        ProviderManager providerManager =
                new ProviderManager(Collections.singletonList(customEmailAuthenticationProvider));
        filter.setAuthenticationManager(providerManager);
        // HttpSecurity中定义总是失效，可能是要绑定到过滤器？
        filter.setAuthenticationSuccessHandler(new CustomAuthenticationSuccessHandler());
        filter.setAuthenticationFailureHandler(new CustomAuthenticationFailHandler());
        return filter;
    }


    // 对存储到数据库的密码进行加密
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 自定义session registry
    @PostConstruct
    @Bean
    public SpringSessionBackedSessionRegistry<S> sessionRegistry() {
        return new SpringSessionBackedSessionRegistry<>(this.sessionRepository);
    }


}