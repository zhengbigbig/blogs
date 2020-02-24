package hello.configuration.security.listener;

import com.alibaba.fastjson.JSON;
import lombok.extern.java.Log;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

@WebListener
@Service
@Log
public class SessionListener implements HttpSessionAttributeListener {
    @Inject
    private SessionRegistry sessionRegistry;

    @Override
    public void attributeAdded(HttpSessionBindingEvent httpSessionBindingEvent) {
        HttpSession session = httpSessionBindingEvent.getSession();
        log.info(httpSessionBindingEvent.getName() + " add-Session: " + session.getId());
        log.info(JSON.toJSONString(sessionRegistry.getAllPrincipals()));
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent httpSessionBindingEvent) {
        HttpSession session = httpSessionBindingEvent.getSession();
        log.info(httpSessionBindingEvent.getName() + " remove-Session: " + session.getId());
        log.info(JSON.toJSONString(sessionRegistry.getAllPrincipals()));
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent httpSessionBindingEvent) {
        log.info("--attributeReplaced--");
        String oldName = httpSessionBindingEvent.getName();
        log.info("replaceSession::" + "oldName: " + oldName + "--oldSession: " + httpSessionBindingEvent.getSession().getId());
        HttpSession session = httpSessionBindingEvent.getSession();
        log.info("newName---:" + session.getAttribute(oldName) + "--newSession:" + "--" + httpSessionBindingEvent.getSession().getId());
        log.info(JSON.toJSONString(sessionRegistry.getAllPrincipals()));
    }
}