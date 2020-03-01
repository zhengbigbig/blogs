package hello.configuration.security.handler;

import hello.configuration.security.provider.token.JwtAuthenticationToken;
import hello.service.impl.UserServiceImpl;
import hello.utils.requests.JwtUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static hello.utils.requests.JwtUtils.TOKEN_PARAMETER.EXPIRATION;
import static hello.utils.requests.JwtUtils.TOKEN_PREFIX;

public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final int tokenRefreshInterval = 300;  //刷新间隔5分钟

    @Inject
    private UserServiceImpl userService;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String jwt = ((JwtAuthenticationToken) authentication).getToken();
        Date o = (Date) JwtUtils.decodeToken(jwt, EXPIRATION);
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        boolean shouldRefresh = shouldTokenRefresh(o);
        // 是否需要刷新token，若需要则刷新，实际上，redis会将过期的token清理，因为前面设置了过期时间
        if (shouldRefresh) {
            // 删除redis中的，再重新创建，若是多端，则逻辑重写
            userService.deleteUserLoginInfoToRedis(principal.getUsername());
            String newToken = userService.saveUserLoginToRedis(principal);
            response.setHeader("Authorization", TOKEN_PREFIX + newToken);
        }
    }

    protected boolean shouldTokenRefresh(Date issueAt) {
        LocalDateTime issueTime = LocalDateTime.ofInstant(issueAt.toInstant(), ZoneId.systemDefault());
        return LocalDateTime.now().minusSeconds(tokenRefreshInterval).isAfter(issueTime);
    }

}
