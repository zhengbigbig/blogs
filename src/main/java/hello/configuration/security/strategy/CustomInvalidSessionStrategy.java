package hello.configuration.security.strategy;

import com.google.common.collect.ImmutableMap;
import hello.utils.requests.RequestUtils;
import org.springframework.security.web.session.InvalidSessionStrategy;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomInvalidSessionStrategy implements InvalidSessionStrategy {

    @Override
    public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ImmutableMap<String, Object> message = ImmutableMap.of(
                "msg", "session无效，请重新登录",
                "status", "fail"

        );
        RequestUtils.sendMessageToResponse(response, message, 200, "session invalid");
    }
}