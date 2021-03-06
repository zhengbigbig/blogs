package hello.utils.requests;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import lombok.extern.java.Log;
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

@Log
public class RequestUtils {
    private static final String LOGIN_URL = "/login";

    /**
     * TODO 判断是否是ajax请求
     *
     * @param request request
     * @return boolean
     */
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


    /**
     * TODO 自定义返回http响应
     *
     * @param response       HttpServletResponse
     * @param body           responseBody
     * @param responseStatus httpStatus
     * @param logMessage     log.info
     */
    public static void sendMessageToResponse(HttpServletResponse response, Map<String, Object> body, int responseStatus, String logMessage) {
        try {
            response.setStatus(responseStatus);
            response.setCharacterEncoding("utf-8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String resBody = JSON.toJSONString(body);
            PrintWriter printWriter = response.getWriter();
            printWriter.print(resBody);
            printWriter.flush();
            printWriter.close();
        } catch (IOException e) {
            log.info(logMessage);
        }
    }


    /**
     * TODO 需要忽视的安全的文件资源
     *
     * @param request request
     * @return boolean
     */
    public static boolean isSecurityAccessDecisionToIgnore(HttpServletRequest request) {
        //OrRequestMatcher or组合多个RequestMatcher
        OrRequestMatcher patternMatcher = new OrRequestMatcher(
                new AntPathRequestMatcher(LOGIN_URL, HttpMethod.POST.name())
        );
        return patternMatcher.matches(request);
    }


    public static Map<String, Object> getBodyFromRequest(HttpServletRequest request) throws IOException {
        RequestWrapper requestWrapper = new RequestWrapper(request);
        String bodyParam = IOUtils.toString(requestWrapper.getInputStream(), Charset.defaultCharset());
        return JSONObject.parseObject(bodyParam);
    }

    /**
     * TODO 认证失败处理 401 403
     *
     * @param request        request
     * @param response       response
     * @param msg            认证信息
     * @param scUnauthorized 状态码
     */
    public static void sendMessageToResponseWhenReject(HttpServletRequest request, HttpServletResponse response, String msg, int scUnauthorized) {
        ImmutableMap<String, Object> message = ImmutableMap.of(
                "uri", request.getRequestURI(),
                "msg", msg,
                "status", "error"
        );
        RequestUtils.sendMessageToResponse(response, message, scUnauthorized, "无权限");
    }

}
