package hello.controller;

import hello.entity.result.NormalResult;
import hello.entity.result.ObjectResult;
import hello.service.SessionService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

@RestController
@RequestMapping("/session")
public class SessionController {
    private final SessionService sessionService;

    @Inject
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/getAllOnlineUsers")
    @ResponseBody
    public ObjectResult getAllOnline() {
        return sessionService.getAllSessions();
    }

    @GetMapping("/removeSession/{username}")
    @ResponseBody
    public NormalResult removeSessionSingleUser(@PathVariable("username") String username) {
        return sessionService.invalidateSession(username);
    }

}
