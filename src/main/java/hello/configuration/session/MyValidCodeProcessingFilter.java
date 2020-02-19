package hello.configuration.session;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.entity.result.LoginResult;
import hello.utils.RequestWrapper;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

@Log
public class MyValidCodeProcessingFilter extends OncePerRequestFilter {
    private final SessionRegistry sessionRegistry;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public MyValidCodeProcessingFilter(SessionRegistry sessionRegistry, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.sessionRegistry = sessionRegistry;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        String auth = request.getHeader("Authorization");
        RequestWrapper requestWrapper = new RequestWrapper(request);
        String bodyParam = IOUtils.toString(requestWrapper.getInputStream(), Charset.defaultCharset());
        log.info("url: " + request.getRequestURI() + "params:" + bodyParam);
        if (("/auth/login").equals(request.getRequestURI())) {
            try {
                Map<String, Object> map = JSONObject.parseObject(bodyParam);
                String username = (String) map.get("username");
                String password = (String) map.get("password");
                //用户登陆，暂不设置权限
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, bCryptPasswordEncoder.encode(password));
//        SecurityContextHolder.getContext().setAuthentication(authentication);
                //        //用户名密码验证通过后,注册session
                sessionRegistry.registerNewSession(request.getSession().getId(), token.getPrincipal());
                filterChain.doFilter(requestWrapper, response);
            } catch (Exception e) {
                LoginResult loginResult = LoginResult.failure("invalid request");
                response.setContentType("application/json; charset=utf-8");
                response.setCharacterEncoding("UTF-8");

                String userJson = convertObjectToJson(loginResult);
                OutputStream out = response.getOutputStream();
                out.write(userJson.getBytes("UTF-8"));
                out.flush();
            }

        } else {
            filterChain.doFilter(requestWrapper, response);
        }
    }

    public String convertObjectToJson(Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

}
