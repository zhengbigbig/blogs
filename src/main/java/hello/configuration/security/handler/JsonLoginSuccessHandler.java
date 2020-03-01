package hello.configuration.security.handler;

import hello.service.impl.UserServiceImpl;
import hello.utils.requests.RequestUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static hello.utils.requests.JwtUtils.TOKEN_PREFIX;

public class JsonLoginSuccessHandler implements AuthenticationSuccessHandler {
    private UserServiceImpl userService;

    public JsonLoginSuccessHandler(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        UserDetails principal = (UserDetails) authentication.getPrincipal();

        // 删除redis中之前的重新生成
        userService.deleteUserLoginInfoToRedis(principal.getUsername());
        String token = userService.saveUserLoginToRedis(principal);

        response.setHeader("Authorization", TOKEN_PREFIX + token);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("msg", token);
        RequestUtils.sendMessageToResponse(response, result, 200, "登录成功 token:" + token);
    }

}
