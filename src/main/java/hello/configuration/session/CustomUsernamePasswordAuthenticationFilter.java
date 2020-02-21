package hello.configuration.session;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    public static final String USERNAME = "j_username";
    public static final String PASSWORD = "j_password";
    /**
     * @Description:用户登录验证方法入口
     * @param :args
     * @return
     * @throws Exception
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {

        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException(
                    "Authentication method not supported: "
                            + request.getMethod());
        }
        String username = this.obtainUsername(request);
        String password = this.obtainPassword(request);
        // 加密密码(根据“密码{用户名})进行加密
        // String sh1Password = password + "{" + username + "}";
        // PasswordEncoder passwordEncoder = new
        // StandardPasswordEncoderForSha1();
        // String result = passwordEncoder.encode(sh1Password);
        // UserInfo userDetails = (UserInfo)
        // userDetailsService.loadUserByUsername(username);
        if (username == null) {
            username = "";
        }

        if (password == null) {
            password = "";
        }

        username = username.trim();

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                username, password);

        // Allow subclasses to set the "details" property
        this.setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);

    }



    /**
     * @Description:获取密码
     * @param :args
     * @return
     * @throws Exception
     */
    @Override
    protected String obtainPassword(HttpServletRequest request) {
        // TODO Auto-generated method stub
        Object obj = request.getParameter(PASSWORD);
        return null == obj ? "" : obj.toString();
    }

    /**
     * @Description:获取用户名
     * @param :args
     * @return
     * @throws Exception
     */
    @Override
    protected String obtainUsername(HttpServletRequest request) {
        // TODO Auto-generated method stub
        Object obj = request.getParameter(USERNAME);
        return null == obj ? "" : obj.toString().trim().toLowerCase();
    }
}
