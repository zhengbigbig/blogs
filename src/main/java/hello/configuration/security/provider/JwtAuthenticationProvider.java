package hello.configuration.security.provider;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import hello.configuration.security.provider.token.JwtAuthenticationToken;
import hello.service.impl.UserServiceImpl;
import hello.utils.requests.JwtUtils;
import lombok.extern.java.Log;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

@Log
public class JwtAuthenticationProvider implements AuthenticationProvider {
    private UserServiceImpl userService;

    public JwtAuthenticationProvider(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        String token = ((JwtAuthenticationToken) authentication).getToken();

        DecodedJWT jwt;
        // 校验并返回解析结果 验证时效
        try {
            jwt = JwtUtils.verifyToken(token);
        } catch (JWTVerificationException | IllegalArgumentException e) {
            log.info(e.getMessage());
            throw new BadCredentialsException("JWT token verify result: invalid", e);
        }

        // 验证用户
        String username = jwt.getSubject();
        String tokenFromRedis = userService.getUserLoginInfoForRedis(username);

        if (!tokenFromRedis.equals(token)) {
            throw new InternalAuthenticationServiceException(
                    "UserDetailsService returned null, which is an interface contract violation");
        }
        UserDetails userDetails = userService.loadUserByUsername(username);
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(userDetails, token, userDetails.getAuthorities());
        return authenticationToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(JwtAuthenticationToken.class);
    }

}
