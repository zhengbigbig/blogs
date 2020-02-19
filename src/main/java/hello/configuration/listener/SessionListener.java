package hello.configuration.listener;

import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

@WebListener
@Service
@Log
public class SessionListener implements HttpSessionAttributeListener {
    @Override
    public void attributeAdded(HttpSessionBindingEvent httpSessionBindingEvent) {
        log.info("--attributeAdded--");
        HttpSession session = httpSessionBindingEvent.getSession();
        log.info("key----:" + httpSessionBindingEvent.getName());
        log.info("id----:" + session.getId());
        log.info("value---:" + httpSessionBindingEvent.getValue());
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent httpSessionBindingEvent) {
        log.info("--attributeRemoved--");
        HttpSession session = httpSessionBindingEvent.getSession();
        log.info("key----:" + httpSessionBindingEvent.getName());
        log.info("id----:" + session.getId());
        log.info("value---:" + httpSessionBindingEvent.getValue());
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent httpSessionBindingEvent) {
        log.info("--attributeReplaced--");
        String oldName = httpSessionBindingEvent.getName();
        log.info("--old key--" + oldName);
        log.info("--old value--" + httpSessionBindingEvent.getValue());
        log.info("id----:" + httpSessionBindingEvent.getSession().getId());
        HttpSession session = httpSessionBindingEvent.getSession();
        log.info("new value---:" + session.getAttribute(oldName));
        log.info("id----:" + httpSessionBindingEvent.getSession().getId());
    }
}