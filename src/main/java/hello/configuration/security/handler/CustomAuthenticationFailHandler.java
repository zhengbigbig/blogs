package hello.configuration.security.handler;


import com.google.common.collect.ImmutableMap;
import hello.utils.requests.RequestUtils;
import lombok.extern.java.Log;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Log
@Service
public class CustomAuthenticationFailHandler extends SimpleUrlAuthenticationFailureHandler {

    /**
     * TODO 这里返回json 若想重定向如下
     * response.setContentType("text/html;charset=UTF-8");
     * super.setDefaultFailureUrl("/login/index?error=true"); // 登录失败，跳转到登录界面
     * super.onAuthenticationFailure(request, response, exception);
     *
     * @param request   请求
     * @param response  相应
     * @param exception 异常
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.info("登录失败:" + exception.getMessage());
        ImmutableMap<String, Object> message = ImmutableMap.of(
                "uri", request.getRequestURI(),
                "msg", "登录失败" + exception.getMessage(),
                "status", "fail"

        );
        RequestUtils.sendMessageToResponse(response, message, 401, "login fail handler");
    }
}
