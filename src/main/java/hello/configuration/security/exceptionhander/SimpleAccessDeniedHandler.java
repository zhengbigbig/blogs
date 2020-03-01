package hello.configuration.security.exceptionhander;

import hello.utils.requests.RequestUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class SimpleAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        RequestUtils.sendMessageToResponseWhenReject(request, response, "请求失败: " + accessDeniedException.getMessage(), HttpServletResponse.SC_FORBIDDEN);
    }
}