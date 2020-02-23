package hello.configuration.authentication.handler;


import hello.entity.user.User;
import hello.utils.requests.RequestUtils;
import lombok.extern.java.Log;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Log
@Service
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Inject
    private SessionRegistry sessionRegistry;
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    /**
     * TODO 这里返回json 若需要跳转，只需要
     * super.setDefaultTargetUrl("/xxx"); // 设置默认登陆成功的跳转地址
     * super.onAuthenticationSuccess(request, response, authentication);
     *
     * @param request        请求
     * @param response       响应
     * @param authentication 权限信息
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        User user = (User) authentication.getPrincipal();
        sessionRegistry.registerNewSession(request.getSession().getId(), authentication);
        try {
            super.setDefaultTargetUrl("/auth"); // 设置默认登陆成功的跳转地址
            super.onAuthenticationSuccess(request, response, authentication);
        } catch (IOException | ServletException e) {
            result.put("uri", request.getRequestURI());
            result.put("userInfo", user);
            result.put("msg", "登录成功");
            result.put("status", "ok");
            RequestUtils.sendMessageToResponse(response, result, 200, "login success handler");
        }
    }
}
