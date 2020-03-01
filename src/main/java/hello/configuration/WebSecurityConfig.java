package hello.configuration;

import hello.configuration.security.datasource.MyInvocationSecurityMetadataSourceService;
import hello.configuration.security.datasource.RoleBasedVoter;
import hello.configuration.security.handler.*;
import hello.configuration.security.interceptor.JwtAuthenticationFilter;
import hello.configuration.security.interceptor.JwtLoginFilter;
import hello.configuration.security.provider.JwtAuthenticationProvider;
import hello.service.impl.UserServiceImpl;
import lombok.extern.java.Log;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.header.Header;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

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
@ServletComponentScan
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final String[] ignoredWebURI = {
            "/", "/index.html", "/error/**", "/static/**", // 静态资源
            "/js/**", "/css/**", "/fonts/**"
    };

    private final String[] securityUrlPermit = {
            "/auth/**", "/favicon.ico", "/user/**"
    };
    @Inject
    private UserServiceImpl userService;

    // 避免自定义过滤器交给spring，否则失效
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(ignoredWebURI);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // csrf默认是开启的，会导致访问403，需要先关闭，一种跨站请求伪造，对post有效
                .csrf().disable()
                .sessionManagement().disable()  //禁用session
                .formLogin().disable() //禁用form登录
                .cors() //添加跨域配置，在返回头上添加如下
                .and()
                .authorizeRequests()
                // ExpressionUrlAuthorizationConfigurer没有提供FilterSecurityInterceptor的set方法
                // 当然也不能直接在过滤器添加，若添加则会出现俩个FilterSecurityInterceptor，网上很多教程误人子弟
                // 无需访问权限的放行
                .antMatchers(securityUrlPermit).permitAll()
                // 自定义FilterInvocationSecurityMetadataSource
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O postProcess(
                            O fsi) {
                        fsi.setSecurityMetadataSource(mySecurityMetadataSource(fsi.getSecurityMetadataSource()));
                        return fsi;
                    }
                })
                .accessDecisionManager(accessDecisionManager())
                .anyRequest().authenticated() // 默认其他的认证
                .and()
                .headers().addHeaderWriter(new StaticHeadersWriter(Arrays.asList(
                new Header("Access-control-Allow-Origin", "*"),
                new Header("Access-Control-Expose-Headers", "Authorization"))))
                .and() //拦截OPTIONS请求，直接返回header
//                .addFilterAfter(new OptionsRequestFilter(), CorsFilter.class)
                // 添加登录过滤器
//                .apply(new JsonLoginConfigurer<>()).loginSuccessHandler(jsonLoginSuccessHandler())
                // 添加token的过滤器
//                .and()
//                .apply(new JwtLoginConfigurer<>()).tokenValidSuccessHandler(jwtRefreshSuccessHandler()).permissiveRequestUrls("/logout")
                // 401 403 处理
//                .and()
                .exceptionHandling()
                .accessDeniedHandler(new SimpleAccessDeniedHandler())
                .authenticationEntryPoint(new SimpleAuthenticationEntryPoint())
                .and()
                //使用默认的logoutFilter
                .logout()
                .logoutUrl(ConstantConfig.WEB_URL.LOGOUT.getUrl())
                .addLogoutHandler(new CustomLogoutHandler())
                // session
                .and()
                .sessionManagement().disable();
    }


    @Bean
    public FilterRegistrationBean jwtLoginFilter() throws Exception {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        JwtLoginFilter filter = new JwtLoginFilter();
        // 使用自带的manager
        filter.setAuthenticationManager(authenticationManagerBean());
        filter.setAuthenticationFailureHandler(new HttpStatusLoginFailureHandler());
        filter.setSessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy());
        filter.setAuthenticationSuccessHandler(new JsonLoginSuccessHandler(userService));
        getHttp().addFilterAfter(filter, LogoutFilter.class);
        registrationBean.setFilter(filter);
//        registrationBean.addUrlPatterns("*.json");//配置过滤规则
//        registrationBean.setUrlPatterns();
        registrationBean.addInitParameter("excludeUrls", "hahahhhaa");//设置init参数
        registrationBean.setName("securityFilter1");//设置过滤器名称
//        registrationBean.setOrder(0);//执行次序
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean jwtAuthenticationFilter() throws Exception {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        // 使用自带的manager
        filter.setAuthenticationManager(authenticationManagerBean());
        filter.setAuthenticationSuccessHandler(jwtRefreshSuccessHandler());
        filter.setAuthenticationFailureHandler(new HttpStatusLoginFailureHandler());
        filter.setPermissiveUrl(securityUrlPermit);
        getHttp().addFilterBefore(filter, LogoutFilter.class);
        registrationBean.setFilter(filter);
//        registrationBean.addUrlPatterns("*.json");//配置过滤规则
//        registrationBean.setUrlPatterns();

        registrationBean.setName("securityFilter2");//设置过滤器名称
//        registrationBean.setOrder(0);//执行次序
        return registrationBean;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider())
                .authenticationProvider(jwtAuthenticationProvider());
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean("jwtAuthenticationProvider")
    protected AuthenticationProvider jwtAuthenticationProvider() {
        return new JwtAuthenticationProvider(userService);
    }

    @Bean("daoAuthenticationProvider")
    protected AuthenticationProvider daoAuthenticationProvider() throws Exception {
        //这里会默认使用BCryptPasswordEncoder比对加密后的密码，注意要跟createUser时保持一致
        //
        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
        daoProvider.setPasswordEncoder(bCryptPasswordEncoder());
        daoProvider.setUserDetailsService(userService);
        return daoProvider;
    }


    @Bean
    protected JsonLoginSuccessHandler jsonLoginSuccessHandler() {
        return new JsonLoginSuccessHandler(userService);
    }

    @Bean
    protected JwtRefreshSuccessHandler jwtRefreshSuccessHandler() {
        return new JwtRefreshSuccessHandler();
    }


    // 对存储到数据库的密码进行加密
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // http.cors() 开启后进行跨域设置
    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "HEAD", "OPTION"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.addExposedHeader("Authorization");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 资源管理,决策放行
     *
     * @return FilterInvocationSecurityMetadataSource
     */
    @Bean
    public FilterInvocationSecurityMetadataSource mySecurityMetadataSource(FilterInvocationSecurityMetadataSource filterInvocationSecurityMetadataSource) {
        MyInvocationSecurityMetadataSourceService metadataSourceService = new MyInvocationSecurityMetadataSourceService(filterInvocationSecurityMetadataSource);
        return metadataSourceService;
    }


    @Bean
    public AccessDecisionManager accessDecisionManager() {
        List<AccessDecisionVoter<? extends Object>> decisionVoters
                = Arrays.asList(
                new WebExpressionVoter(),
                new AuthenticatedVoter(),
                new RoleBasedVoter()
        );
        return new UnanimousBased(decisionVoters);
    }
}