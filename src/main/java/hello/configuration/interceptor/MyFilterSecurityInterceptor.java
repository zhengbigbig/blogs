package hello.configuration.interceptor;

import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.access.intercept.AbstractSecurityInterceptor;
import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.*;
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
@Service
public class MyFilterSecurityInterceptor extends AbstractSecurityInterceptor implements Filter {
    private FilterInvocationSecurityMetadataSource securityMetadataSource;

    @Inject
    public MyFilterSecurityInterceptor(FilterInvocationSecurityMetadataSource securityMetadataSource) {
        this.securityMetadataSource = securityMetadataSource;
    }

    @Inject
    public void setAccessDecisionManager(MyAccessDecisionManager myAccessDecisionManager) {
        super.setAccessDecisionManager(myAccessDecisionManager);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        FilterInvocation fi = new FilterInvocation(servletRequest, servletResponse, filterChain);
        invoke(fi);
    }

    private void invoke(FilterInvocation fi) throws IOException, ServletException {
        // 验证Context中的Authentication和目标url所需权限是否匹配，匹配则通过，不通过则抛出异常
        InterceptorStatusToken token = super.beforeInvocation(fi);
        try {
            //执行下一个拦截器 请求真正的controller
            fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
        } finally {
            super.afterInvocation(token, null);
        }
    }

    // 定义Object类型
    @Override
    public Class<?> getSecureObjectClass() {
        return FilterInvocation.class;
    }

    // 获取目标url所需要的权限， 该类实现FilterInvocationSecurityMetadataSource接口的方法
    @Override
    public SecurityMetadataSource obtainSecurityMetadataSource() {
        return this.securityMetadataSource;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}
