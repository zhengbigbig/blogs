package hello.configuration.security.interceptor;

import hello.entity.user.Permission;
import hello.mapper.PermissionMapper;
import lombok.extern.java.Log;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.util.AntPathMatcher;

import javax.inject.Inject;
import java.util.*;

// 用来储存请求与权限的对应关系
@Log
public class MyInvocationSecurityMetadataSourceService implements FilterInvocationSecurityMetadataSource {
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private HashMap<String, Collection<ConfigAttribute>> allUrlRoleMap = null;
    private FilterInvocationSecurityMetadataSource superMetadataSource;
    @Inject
    private PermissionMapper permissionMapper;

    public MyInvocationSecurityMetadataSourceService(FilterInvocationSecurityMetadataSource superMetadataSource) {
        this.superMetadataSource = superMetadataSource;
    }

    /*
     * 加载权限表中所有权限
     */
    public void loadAllPermissionResource() {
        allUrlRoleMap = new HashMap<>(16);
        List<Permission> permissions = permissionMapper.findAllPermission();
        // 某个资源 可以被哪些角色访问,用权限的getUrl() 作为map的key，用ConfigAttribute的集合作为 value，
        for (Permission permission : permissions) {
            String url = permission.getUrl();
            String name = permission.getName();
            ConfigAttribute role = new SecurityConfig(name);
            /* 此处只添加了名字，其实还可以添加更多权限的信息，
             * 例如请求方法到ConfigAttribute的集合中去。
             * 此处添加的信息将会作为MyAccessDecisionManager类的decide的第三个参数。
             */
            if (allUrlRoleMap.containsKey(url)) {
                allUrlRoleMap.get(url).add(role);
            } else {
                List<ConfigAttribute> list = new ArrayList<>();
                list.add(role);
                allUrlRoleMap.put(url, list);
            }
        }
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
        // TODO 加载数据库所有权限，当然可以只加载一个
        if (allUrlRoleMap == null) {
            loadAllPermissionResource();
        }

        FilterInvocation fi = (FilterInvocation) object;
        String url = fi.getRequestUrl();

        for (Map.Entry<String, Collection<ConfigAttribute>> entry : allUrlRoleMap.entrySet()) {
            if (antPathMatcher.match(entry.getKey(), url)) {
                return entry.getValue();
            }
        }

        //  返回代码定义的默认配置
        return superMetadataSource.getAttributes(object);
    }

    // Spring容器启动时自动调用, 一般把所有请求与权限的对应关系也要在这个方法里初始化, 保存在一个属性变量里
    // 默认配置
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
