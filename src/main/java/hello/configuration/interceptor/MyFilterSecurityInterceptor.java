package hello.configuration.interceptor;

import com.google.common.collect.ImmutableMap;
import hello.dao.PermissionMapper;
import hello.utils.ApiRRException;
import hello.utils.requests.RequestUtils;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.access.intercept.AbstractSecurityInterceptor;
import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

// AbstractSecurityInterceptor 是一个实现了对受保护对象的访问进行拦截的抽象类
/* beforeInvocation()方法实现了对访问受保护对象的权限校验
 * finallyInvocation()方法用于实现受保护对象请求完毕后的一些清理工作，主要是如果在beforeInvocation()中改变了SecurityContext，则在finallyInvocation()中需要将其恢复为原来的SecurityContext，该方法的调用应当包含在子类请求受保护资源时的finally语句块中
 * afterInvocation()方法实现了对返回结果的处理，在注入了AfterInvocationManager的情况下默认会调用其decide()方法
 * 流程：
 * 1.先将正在请求调用的受保护对象传递给beforeInvocation()方法进行权限鉴定。
 * 2.权限鉴定失败就直接抛出异常了。
 * 3.鉴定成功将尝试调用受保护对象，调用完成后，不管是成功调用，还是抛出异常，都将执行finallyInvocation()。
 * 4.如果在调用受保护对象后没有抛出异常，则调用afterInvocation()。


 */

public class MyFilterSecurityInterceptor extends AbstractSecurityInterceptor implements Filter {
    private static final String LOGIN_URL = "/auth/login";

    private RequestMatcher requestMatcher;
    private PermissionMapper permissionMapper;

    public MyFilterSecurityInterceptor(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
        //OrRequestMatcher or组合多个RequestMatcher
        this.requestMatcher = new OrRequestMatcher(
                new AntPathRequestMatcher(LOGIN_URL, HttpMethod.POST.name())
        );
    }

    @Override
    public void setAccessDecisionManager(AccessDecisionManager accessDecisionManager) {
        super.setAccessDecisionManager(accessDecisionManager);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (requestMatcher.matches((HttpServletRequest) servletRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            FilterInvocation fi = new FilterInvocation(servletRequest, servletResponse, filterChain);
            invoke(fi);
        }

    }

    private void invoke(FilterInvocation fi) throws IOException, ServletException {
        try {

            InterceptorStatusToken token = super.beforeInvocation(fi);

            try {
                //执行下一个拦截器 请求真正的controller
                fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
            } finally {
                super.finallyInvocation(token);
            }
            super.afterInvocation(token, null);
        } catch (ApiRRException e) {
            ImmutableMap<String, String> message = ImmutableMap.of(
                    "uri", fi.getRequest().getRequestURI(),
                    "msg", e.getMsg(),
                    "status", "error"
            );
            RequestUtils.sendMessageToResponse(
                    fi.getResponse(),
                    message,
                    e.getError()
            );
        }


    }

    // 定义Object类型
    @Override
    public Class<?> getSecureObjectClass() {
        return FilterInvocation.class;
    }

    @Override
    public SecurityMetadataSource obtainSecurityMetadataSource() {
        return new MyInvocationSecurityMetadataSourceService(permissionMapper);
    }

    // 获取目标url所需要的权限， 该类实现FilterInvocationSecurityMetadataSource接口的方法

    @Override
    public void destroy() {

    }
}
