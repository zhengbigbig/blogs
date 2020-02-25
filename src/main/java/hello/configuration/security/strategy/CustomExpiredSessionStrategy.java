package hello.configuration.security.strategy;

import com.google.common.collect.ImmutableMap;
import hello.utils.requests.RequestUtils;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomExpiredSessionStrategy implements SessionInformationExpiredStrategy {

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException, ServletException {
        HttpServletResponse response = event.getResponse();
        ImmutableMap<String, Object> message = ImmutableMap.of(
                "msg", "登录失效，请重新登录",
                "status", "warming"

        );
        RequestUtils.sendMessageToResponse(response, message, 200, "session expired");
    }
}
