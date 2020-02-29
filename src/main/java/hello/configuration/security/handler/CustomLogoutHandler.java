package hello.configuration.security.handler;

import com.google.common.collect.ImmutableMap;
import hello.service.impl.UserServiceImpl;
import hello.utils.requests.RequestUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CustomLogoutHandler implements LogoutHandler {
    @Inject
    private UserServiceImpl userService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        Boolean clearToken = clearToken(authentication);
        RequestUtils.sendMessageToResponse(response,
                ImmutableMap.of(
                        "msg", clearToken ? "注销成功" : "注销失败",
                        "status", clearToken ? "ok" : "fail"),
                clearToken ? 200 : 401, "退出"
        );

    }

    private Boolean clearToken(Authentication authentication) {
        if (authentication == null) {
            return true;
        }
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String username;
        if (user != null && (username = user.getUsername()) != null) {
            // 删除redis中token
            return userService.deleteUserLoginInfoToRedis(username);
        }

        return true;
    }
}
