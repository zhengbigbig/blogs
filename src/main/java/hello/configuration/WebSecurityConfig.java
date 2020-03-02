package hello.configuration;

import hello.configuration.security.AuthenticationFilterConfig;
import hello.configuration.security.cors.CorsConfiguration;
import hello.configuration.security.exceptionhander.SimpleAccessDeniedHandler;
import hello.configuration.security.exceptionhander.SimpleAuthenticationEntryPoint;
import hello.configuration.security.handler.CustomLogoutHandler;
import hello.configuration.security.interceptor.MyInvocationSecurityMetadataSourceService;
import hello.configuration.security.interceptor.RoleBasedVoter;
import hello.configuration.security.provider.JwtAuthenticationProvider;
import hello.utils.SystemPropertiesEnv;
import lombok.extern.java.Log;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.header.Header;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

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
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Inject
    private SystemPropertiesEnv systemPropertiesEnv;
    @Inject
    private CorsConfiguration corsConfiguration;
    @Inject
    private AuthenticationFilterConfig authenticationFilterConfig;
    @Inject
    private JwtAuthenticationProvider jwtAuthenticationProvider;
    @Inject
    private DaoAuthenticationProvider daoAuthenticationProvider;

    // 避免自定义过滤器交给spring，否则失效
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(systemPropertiesEnv.getWebIgnore());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // csrf默认是开启的，会导致访问403，需要先关闭，一种跨站请求伪造，对post有效
        http
                .csrf().disable()
                .sessionManagement().disable()  //禁用session
                .formLogin().disable() //禁用form登录
                .exceptionHandling()
                .accessDeniedHandler(new SimpleAccessDeniedHandler())
                .authenticationEntryPoint(new SimpleAuthenticationEntryPoint())
                .and()
                .authorizeRequests()
                // ExpressionUrlAuthorizationConfigurer没有提供FilterSecurityInterceptor的set方法
                // 当然也不能直接在过滤器添加，若添加则会出现俩个FilterSecurityInterceptor，网上很多教程误人子弟
                // 无需访问权限的放行
                .antMatchers(systemPropertiesEnv.getSecurityPermit()).permitAll()
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
                //使用默认的logoutFilter
                .logout()
                .logoutUrl(ConstantConfig.WEB_URL.LOGOUT.getUrl())
                .addLogoutHandler(new CustomLogoutHandler())
                .and()
                .apply(authenticationFilterConfig);
        if (systemPropertiesEnv.getCors()) {
            http.cors() //添加跨域配置，在返回头上添加如下
                    .and()
                    .headers().addHeaderWriter(new StaticHeadersWriter(Arrays.asList(
                    new Header("Access-control-Allow-Origin", "*"),
                    new Header("Access-Control-Expose-Headers", "Authorization"))))
                    .and()
                    //拦截OPTIONS请求，直接返回header
                    .apply(corsConfiguration);

        }
    }

    /**
     * 鉴权
     *
     * @return AuthenticationManager
     * @throws Exception NullException
     */
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    @Lazy
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(jwtAuthenticationProvider)
                .authenticationProvider(daoAuthenticationProvider);
    }


    /**
     * 资源管理,决策放行
     *
     * @param filterInvocationSecurityMetadataSource 参入放行参数
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