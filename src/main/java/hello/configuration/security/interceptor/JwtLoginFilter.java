package hello.configuration.security.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static hello.configuration.ConstantConfig.WEB_URL.LOGIN;

/**
 * Created by zhengzhiheng on 2020/2/28 11:53 上午
 * Description:
 */


// 重写，便于自定义接受的参数
@Log
public class JwtLoginFilter extends AbstractAuthenticationProcessingFilter {
    private ThreadLocal<Map<String, String>> threadLocal = new ThreadLocal<>();

    public JwtLoginFilter() {
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
            username = params.get("username").trim();
            password = params.get("password").trim();
        } catch (Exception e) {
            log.info("登录传参出错");
            throw new InternalAuthenticationServiceException("Failed to get the your parameter");
        }

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
