package hello.utils.requests;


import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Map;

public class RequestUtils {
    private static final String LOGIN_URL = "/login";

    public static boolean isAjaxRequest(HttpServletRequest request) {

        String accept = request.getHeader("accept");
        if (accept != null && accept.indexOf("application/json") != -1) {
            return true;
        }

        String xRequestedWith = request.getHeader("X-Requested-With");
        if (xRequestedWith != null && xRequestedWith.indexOf("XMLHttpRequest") != -1) {
            return true;
        }

        String uri = request.getRequestURI();
        if (StringUtils.equalsAnyIgnoreCase(uri, ".json", ".xml")) {
            return true;
        }

        String ajax = request.getParameter("__ajax");
        if (StringUtils.equalsAnyIgnoreCase(ajax, "json", "xml")) {
            return true;
        }

        return false;
    }

    public static void sendMessageToResponse(
            HttpServletResponse response,
            Map<String, String> body, int responseStatus
    ) throws IOException {
        response.setStatus(responseStatus);
        response.setCharacterEncoding("utf-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ObjectMapper objectMapper = new ObjectMapper();
        String resBody = objectMapper.writeValueAsString(body);
        PrintWriter printWriter = response.getWriter();
        printWriter.print(resBody);
        printWriter.flush();
        printWriter.close();
    }

    public static boolean isSecurityAccessDecisionToIgnore(
            HttpServletRequest request) {
        //OrRequestMatcher or组合多个RequestMatcher
        OrRequestMatcher patternMatcher = new OrRequestMatcher(
                new AntPathRequestMatcher(LOGIN_URL, HttpMethod.POST.name())
        );
        return patternMatcher.matches(request);
    }

    public static Map<String, Object> getBodyFromRequest(HttpServletRequest request, String want) throws IOException {
        RequestWrapper requestWrapper = new RequestWrapper(request);
        String bodyParam = IOUtils.toString(requestWrapper.getInputStream(), Charset.defaultCharset());
        return JSONObject.parseObject(bodyParam);
    }

}
