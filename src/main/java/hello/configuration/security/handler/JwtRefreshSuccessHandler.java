package hello.configuration.security.handler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hello.configuration.security.provider.token.JwtAuthenticationToken;
import hello.service.impl.UserServiceImpl;
import hello.utils.requests.JwtUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;


import static hello.utils.requests.JwtUtils.TOKEN_PARAMETER.EXPIRATION;

public class JwtRefreshSuccessHandler implements AuthenticationSuccessHandler {

    private static final int tokenRefreshInterval = 300;  //刷新间隔5分钟

	@Inject
    private UserServiceImpl userService;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String jwt = ((JwtAuthenticationToken) authentication).getToken();
        Date o = (Date) JwtUtils.decodeToken(jwt, EXPIRATION);
        boolean shouldRefresh = shouldTokenRefresh(o);
        if (shouldRefresh) {
            String newToken = userService.saveUserLoginToRedis((UserDetails) authentication.getPrincipal());
            response.setHeader("Authorization", newToken);
        }
    }

    protected boolean shouldTokenRefresh(Date issueAt) {
        LocalDateTime issueTime = LocalDateTime.ofInstant(issueAt.toInstant(), ZoneId.systemDefault());
        return LocalDateTime.now().minusSeconds(tokenRefreshInterval).isAfter(issueTime);
    }

}
