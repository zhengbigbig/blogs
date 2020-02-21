package hello.configuration.unauthenticate;

import com.google.common.collect.ImmutableMap;
import hello.utils.requests.RequestUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class SimpleAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        sendMessageToResponseWhenReject(request, response, "没有权限", HttpServletResponse.SC_UNAUTHORIZED);

    }

    public static void sendMessageToResponseWhenReject(HttpServletRequest request, HttpServletResponse response, String msg, int scUnauthorized) throws IOException {
        ImmutableMap<String, String> message = ImmutableMap.of(
                "uri", request.getRequestURI(),
                "msg", msg,
                "status", "error"
        );
        RequestUtils.sendMessageToResponse(response, message, scUnauthorized);
    }
}
