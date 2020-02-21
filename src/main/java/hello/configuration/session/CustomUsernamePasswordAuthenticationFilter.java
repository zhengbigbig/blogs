package hello.configuration.session;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.service.UserService;
import hello.utils.requests.RequestUtils;
import hello.utils.requests.RequestWrapper;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Log
public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private ThreadLocal<Map<String, String>> threadLocal = new ThreadLocal<>();

    private UserDetailsService userService;

    public CustomUsernamePasswordAuthenticationFilter(UserDetailsService userService) {
        this.userService = userService;
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

        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException(
                    "Authentication method not supported: "
                            + request.getMethod());
        }
        String password = this.obtainPassword(request);
        String username = this.obtainUsername(request);

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                username, password);
        this.setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);

    }


    /**
     * @param :args
     * @return
     * @throws Exception
     * @Description:获取密码
     */
    @Override
    protected String obtainPassword(HttpServletRequest request) {
        String password = this.getBodyParams(request).get(super.SPRING_SECURITY_FORM_PASSWORD_KEY);
        if (!StringUtils.isEmpty(password)) {
            return password;
        }
        return super.obtainPassword(request);

    }

    /**
     * @param :args
     * @return
     * @throws Exception
     * @Description:获取用户名
     */
    @Override
    protected String obtainUsername(HttpServletRequest request) {
        String username = this.getBodyParams(request).get(super.SPRING_SECURITY_FORM_USERNAME_KEY);
        if (!StringUtils.isEmpty(username)) {
            return username;
        }
        return super.obtainUsername(request);

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
