package hello.configuration.interceptor;

import hello.dao.PermissionMapper;
import hello.entity.user.Permission;
import lombok.extern.java.Log;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

// 用来储存请求与权限的对应关系
@Log
@Service
public class MyInvocationSecurityMetadataSourceService implements FilterInvocationSecurityMetadataSource {
    private PermissionMapper permissionMapper;

    @Inject
    public MyInvocationSecurityMetadataSourceService(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    /*
    当接收到一个http请求时, filterSecurityInterceptor会调用的方法.
    参数object是一个包含url信息的HttpServletRequest实例.
    这个方法要返回请求该url所需要的所有权限集合
    此处返回的信息将会作为MyAccessDecisionManager类的decide的第三个参数
    todo 此处实现为 若url在数据库则校验，不在则放行
     */
    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {

        HttpServletRequest request = ((FilterInvocation) object).getRequest();
        String url = request.getRequestURI();
        Collection<ConfigAttribute> collection = new LinkedList<>();

        List<Permission> permissions = permissionMapper.findPermissionsByUrl(url);
        for (Permission p : permissions) {
            String name;
            if ((name = p.getName()) != null) {
                collection.add(new SecurityConfig(name));
            }
        }

        //防止数据库中没有数据，不能进行权限拦截
        if (collection.size() < 1) {
            ConfigAttribute configAttribute = new SecurityConfig("NO_NEED_RIGHT");
            collection.add(configAttribute);
        }
        return collection;
    }

    // Spring容器启动时自动调用, 一般把所有请求与权限的对应关系也要在这个方法里初始化, 保存在一个属性变量里
    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    // 指示该类是否能够为指定的方法调用或Web请求提供
    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);

    }
}
