package hello.service;

import hello.entity.result.NormalResult;
import hello.entity.result.ObjectResult;
import hello.entity.user.User;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SessionService {

    private final SessionRegistry sessionRegistry;
    private final UserService userService;

    @Inject
    public SessionService(SessionRegistry sessionRegistry, UserService userService) {
        this.sessionRegistry = sessionRegistry;
        this.userService = userService;
    }


    public NormalResult invalidateSession(String username) {
        List<SessionInformation> allSessions = sessionRegistry.getAllSessions(username, false);
        for (SessionInformation sessionInformation : allSessions) {
            if (sessionInformation == null) {
                return NormalResult.failure("用户会话已失效");
            }
            sessionInformation.expireNow();
            sessionRegistry.removeSessionInformation(sessionInformation.getSessionId());
        }
        return NormalResult.success("下线:" + username + "成功");
    }

    public ObjectResult getAllSessions() {
        List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
        List<User> usersOnline = allPrincipals.stream()
                .map(principal -> userService.getUserByUsernameOrEmail((String) principal))
                .filter(Objects::nonNull)
                .filter(user -> sessionRegistry.getAllSessions(user.getUsername(), false).size() > 0)
                .collect(Collectors.toList());
        if (usersOnline.size() > 0) {
            return ObjectResult.success("获取成功", usersOnline);
        }
        return ObjectResult.failure("当前无用户在线");

    }
}
