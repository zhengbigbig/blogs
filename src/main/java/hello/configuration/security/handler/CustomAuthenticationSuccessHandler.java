package hello.configuration.security.handler;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import hello.configuration.ConstantConfig;
import hello.entity.user.User;
import hello.utils.TimeUtils;
import hello.utils.requests.RequestUtils;
import lombok.extern.java.Log;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Log
@Service
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final SessionRegistry sessionRegistry;

    public CustomAuthenticationSuccessHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    /**
     * TODO 这里返回json 若需要跳转，只需要
     * super.setDefaultTargetUrl("/xxx"); // 设置默认登陆成功的跳转地址
     * super.onAuthenticationSuccess(request, response, authentication);
     *
     * @param request        请求
     * @param response       响应
     * @param authentication 权限信息
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 成功登录保存到session仓库
        sessionRegistry.registerNewSession(request.getSession().getId(), authentication);
        // 处理返回前端数据
        User user = (User) authentication.getPrincipal();
        Map<String, Object> stringObjectMap = parseUserToMapResult(user);
        RequestUtils.sendMessageToResponse(response, stringObjectMap, 200, "login success handler");
    }

    private Map<String, Object> parseUserToMapResult(User user) {
        Map<String, Object> result = new HashMap<>();
        String createdAt = TimeUtils.formatInstantToDateString(user.getCreatedAt(), ConstantConfig.DATE_FORMAT_ZH);
        String updatedAt = TimeUtils.formatInstantToDateString(user.getUpdatedAt(), ConstantConfig.DATE_FORMAT_ZH);
        User userNotNullProperty = JSONObject.parseObject(JSON.toJSONString(user), User.class);
        ImmutableMap<Object, Object> userInfo = ImmutableMap.builder()
                .put("id", userNotNullProperty.getId())
                .put("username", userNotNullProperty.getUsername())
                .put("avatar", userNotNullProperty.getAvatar())
                .put("email", userNotNullProperty.getEmail())
                .put("sex", userNotNullProperty.getSex())
                .put("summary", userNotNullProperty.getSummary())
                .put("profession", userNotNullProperty.getProfession())
                .put("address", userNotNullProperty.getAddress())
                .put("technologyStack", userNotNullProperty.getTechnologyStack())
                .put("createdAt", createdAt)
                .put("updatedAt", updatedAt)
                .put("roles", userNotNullProperty.getRoles())
                .build();
        result.put("data", userInfo);
        result.put("msg", "登录成功");
        result.put("status", "ok");
        return result;
    }
}
