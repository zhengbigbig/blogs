package hello.configuration.authentication.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.configuration.authentication.token.EmailLoginAuthenticationToken;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

// 重写，便于自定义接受的参数
@Log
public class CustomUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private ThreadLocal<Map<String, String>> threadLocal = new ThreadLocal<>();

    public CustomUsernamePasswordAuthenticationFilter() {
        super(new AntPathRequestMatcher("/login", "POST"));
    }

    /**
     * @param :args
     * @return
     * @throws Exception
     * @Description:用户登录验证方法入口
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        String password = "";
        String username = "";
        try {
            Map<String, String> params = getBodyParams(request);
            username = params.get("username");
            password = params.get("password");
        } catch (Exception e) {
            log.info("登录传参出错");
            throw new InternalAuthenticationServiceException("Failed to get the your parameter");
        }
        EmailLoginAuthenticationToken authRequest = new EmailLoginAuthenticationToken(username, password);
        authRequest.setSession(request.getSession().getId());
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    /**
     * 获取body参数  body中的参数只能获取一次
     *
     * @param request
     * @return
     */
    private Map<String, String> getBodyParams(HttpServletRequest request) {
        Map<String, String> bodyParams = threadLocal.get();
        if (bodyParams == null) {
            ObjectMapper objectMapper = new ObjectMapper();
            try (InputStream is = request.getInputStream()) {
                bodyParams = objectMapper.readValue(is, Map.class);
            } catch (IOException e) {
            }
            if (bodyParams == null) {
                bodyParams = new HashMap<>();
            }
            threadLocal.set(bodyParams);
        }

        return bodyParams;
    }
}
