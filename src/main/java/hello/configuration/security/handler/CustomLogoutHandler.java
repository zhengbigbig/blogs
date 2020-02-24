package hello.configuration.security.handler;

import com.google.common.collect.ImmutableMap;
import hello.configuration.ConstantConfig;
import hello.utils.requests.RequestUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomLogoutHandler implements LogoutHandler {
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            redirectStrategy.sendRedirect(request, response, ConstantConfig.WEB_URL.LOGOUT_REDIRECT.getUrl());
        } catch (IOException e) {
            RequestUtils.sendMessageToResponse(response,
                    ImmutableMap.of(
                            "msg", "注销失败",
                            "status", "fail"),
                    500, "出错了"
            );
        }
    }
}
