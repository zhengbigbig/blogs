package hello.configuration.security.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static hello.configuration.ConstantConfig.WEB_URL.LOGIN;

// 重写，便于自定义接受的参数
@Log
public class CustomUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private ThreadLocal<Map<String, String>> threadLocal = new ThreadLocal<>();
    @Inject
    private SessionRegistry sessionRegistry;
    public CustomUsernamePasswordAuthenticationFilter() {
        super(new AntPathRequestMatcher(LOGIN.getUrl(), LOGIN.getMethod()));
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
        // 以下代码后续移除，用户在别地已登录，先登录虽然不会成功登记sessionId，但是需要提前拦截返回

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
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
                log.info("userFilter read body exception");
            }
            if (bodyParams == null) {
                bodyParams = new HashMap<>();
            }
            threadLocal.set(bodyParams);
        }

        return bodyParams;
    }


}
