//package hello.configuration.security.datasource;
//
//import lombok.extern.java.Log;
//import org.springframework.security.access.AccessDecisionManager;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.access.ConfigAttribute;
//import org.springframework.security.authentication.InsufficientAuthenticationException;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.stereotype.Component;
//
//import java.util.Collection;
//import java.util.Iterator;
//
//@Log
//public class MyAccessDecisionManager implements AccessDecisionManager {
//
//    /**
//     * 判定是否拥有权限的决策方法
//     *
//     * @param authentication   UserService中添加到 GrantedAuthority 对象中的权限信息集合
//     * @param object           包含客户端发起的请求的requset信息，可转换为 HttpServletRequest request = ((FilterInvocation) object).getHttpRequest()
//     * @param configAttributes 为MyInvocationSecurityMetadataSource的getAttributes(Object object)返回的结果
//     *                         判断用户请求的url是否在权限表，不在则放行，在则返回给decide方法来判断是否拥有权限
//     * @throws AccessDeniedException
//     * @throws InsufficientAuthenticationException 必须在登录后才能正常决策
//     */
//    // 想让其放行 可设置 ROLE_ANONYMOUS
//    @Override
//    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException {
//        Iterator<ConfigAttribute> iterator = configAttributes.iterator();
//        while (iterator.hasNext()) {
//            ConfigAttribute ca = iterator.next();
//            //当前请求需要无权限url
//            String needRole = ca.getAttribute();
//
//            //当前用户所具有的权限,放行
//            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
//            for (GrantedAuthority authority : authorities) {
//                if (authority.getAuthority().equals(needRole)) {
//                    return;
//                }
//            }
//        }
//        throw new AccessDeniedException("权限不足!");
//    }
//
//    /**
//     * 表示此AccessDecisionManager是否能够处理传递的ConfigAttribute呈现的授权请求
//     */
//    @Override
//    public boolean supports(ConfigAttribute attribute) {
//        return true;
//    }
//
//    /**
//     * 表示当前AccessDecisionManager实现是否能够为指定的安全对象（方法调用或Web请求）提供访问控制决策
//     */
//    @Override
//    public boolean supports(Class<?> clazz) {
//        return true;
//    }
//
//
//}
