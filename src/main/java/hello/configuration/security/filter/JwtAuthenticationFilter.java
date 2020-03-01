package hello.configuration.security.filter;

import cn.hutool.core.util.ArrayUtil;
import com.auth0.jwt.exceptions.JWTVerificationException;
import hello.configuration.security.provider.token.JwtAuthenticationToken;
import hello.utils.SystemPropertiesEnv;
import hello.utils.requests.JwtUtils;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Created by zhengzhiheng on 2020/2/28 1:38 下午
 * Description:
 */
@Log
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private AuthenticationManager authenticationManager;

    private AuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
    private AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
    private SystemPropertiesEnv systemPropertiesEnv;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        //header没带token的，直接放过，因为部分url匿名用户也可以访问
        //如果需要不支持匿名用户的请求没带token，这里放过也没问题，因为SecurityContext中没有认证信息，后面会被权限控制模块拦截
        String tokenHeader = checkTheHeaderReturnTokenEntity(request);

        String token = getJwtTokenAfterRemovePrefix(tokenHeader);
        // token 初步校验有问题直接放行，去走下个过滤器
        if (StringUtils.isBlank(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authResult = null;
        AuthenticationException failed = null;

        try {
            JwtAuthenticationToken authToken = new JwtAuthenticationToken(token);
            authResult = this.authenticationManager.authenticate(authToken);

        } catch (JWTVerificationException e) {
            log.info("JWT format error :" + e.getMessage());
            failed = new InsufficientAuthenticationException("JWT format error", e);
        } catch (InternalAuthenticationServiceException e) {
            log.info("An internal error occurred while trying to authenticate the user. :" + e.getMessage());
            failed = e;
        } catch (AuthenticationException e) {
            // Authentication failed
            failed = e;
        }
        if (authResult != null) {
            successfulAuthentication(request, response, filterChain, authResult);
            // 如果验证未通过，譬如无效的token，但有些接口无需权限的，可以直接判断后转给下一个过滤器
            // 这里仅做思路，因为我们有了WebExpressionVoter可以投票出HttpSecurity中ExpressionUrlAuthorizationConfigurer
            // 下面判断可省略，后续，若想对过滤器进行制定验证，通过getter和setter将参数传入了做校验
        } else if (!permissiveRequest(request)) {
            unsuccessfulAuthentication(request, response, failed);
        }

        filterChain.doFilter(request, response);
    }


    public String checkTheHeaderReturnTokenEntity(HttpServletRequest request) {
        String tokenHeader = request.getHeader(JwtUtils.TOKEN_HEADER);
        return tokenHeader != null && tokenHeader.startsWith(JwtUtils.TOKEN_PREFIX) ?
                tokenHeader : "";
    }

    protected String getJwtTokenAfterRemovePrefix(String tokenHeader) {
        return StringUtils.removeStart(tokenHeader, JwtUtils.TOKEN_PREFIX);
    }

    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response, AuthenticationException failed)
            throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        failureHandler.onAuthenticationFailure(request, response, failed);
    }

    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response, FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        successHandler.onAuthenticationSuccess(request, response, authResult);
    }

    @Inject
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Inject
    public void setSystemPropertiesEnv(SystemPropertiesEnv systemPropertiesEnv) {
        this.systemPropertiesEnv = systemPropertiesEnv;
    }

    protected boolean permissiveRequest(HttpServletRequest request) {
        String[] securityPermit = systemPropertiesEnv.getSecurityPermit();
        if (ArrayUtil.isEmpty(securityPermit)) {
            return false;
        } else {
            for (String url : securityPermit) {
                AntPathRequestMatcher antPathRequestMatcher = new AntPathRequestMatcher(url);
                if (antPathRequestMatcher.matches(request)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setAuthenticationSuccessHandler(
            AuthenticationSuccessHandler successHandler) {
        Assert.notNull(successHandler, "successHandler cannot be null");
        this.successHandler = successHandler;
    }

    public void setAuthenticationFailureHandler(
            AuthenticationFailureHandler failureHandler) {
        Assert.notNull(failureHandler, "failureHandler cannot be null");
        this.failureHandler = failureHandler;
    }
}
