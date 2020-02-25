package hello.configuration.security.listener;

import lombok.extern.java.Log;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.stereotype.Service;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;

@WebListener
@Service
@Log
public class SessionListener extends HttpSessionEventPublisher {
    @Override
    public void sessionCreated(HttpSessionEvent event) {
        log.info(event.getSession().getId() + "::created");
        super.sessionCreated(event);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        log.info(event.getSession().getId() + "::destroyed");
        super.sessionDestroyed(event);
    }
}