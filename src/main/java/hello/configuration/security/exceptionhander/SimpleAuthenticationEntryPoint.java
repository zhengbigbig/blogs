package hello.configuration.security.exceptionhander;

import hello.utils.requests.RequestUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class SimpleAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        RequestUtils.sendMessageToResponseWhenReject(request, response,
                "权限不足:可尝试联系管理员！", HttpServletResponse.SC_UNAUTHORIZED);
    }
}
