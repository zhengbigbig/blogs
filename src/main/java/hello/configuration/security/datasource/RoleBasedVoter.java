package hello.configuration.security.datasource;

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by zhengzhiheng on 2020/2/29 4:27 下午
 * Description:
 */
public class RoleBasedVoter implements AccessDecisionVoter<Object> {


    /**
     * 判定是否拥有权限的决策方法
     *
     * @param authentication   UserService中添加到 GrantedAuthority 对象中的权限信息集合
     * @param object           包含客户端发起的请求的requset信息，可转换为 HttpServletRequest request = ((FilterInvocation) object).getHttpRequest()
     * @param configAttributes 为MyInvocationSecurityMetadataSource的getAttributes(Object object)返回的结果
     *                         判断用户请求的url是否在权限表，不在则放行，在则返回给decide方法来判断是否拥有权限
     * @throws AccessDeniedException
     * @throws InsufficientAuthenticationException 必须在登录后才能正常决策
     * @return
     *  int ACCESS_GRANTED = 1; 承认
     * 	int ACCESS_ABSTAIN = 0; 弃权
     * 	int ACCESS_DENIED = -1; 拒绝
     */
    // 想让其放行 可设置 ROLE_ANONYMOUS
    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) {
        if(authentication == null) {
            return ACCESS_DENIED;
        }
        int result = ACCESS_ABSTAIN;
        Collection<? extends GrantedAuthority> authorities = extractAuthorities(authentication);

        for (ConfigAttribute attribute : configAttributes) {
            if(attribute.getAttribute()==null){
                continue;
            }
            if (this.supports(attribute)) {
                result = ACCESS_DENIED;

                //当前用户所具有的权限,放行
                for (GrantedAuthority authority : authorities) {
                    if (attribute.getAttribute().equals(authority.getAuthority())) {
                        return ACCESS_GRANTED;
                    }
                }
            }
        }
        return result;
    }

    // 当前请求对象的权限
    Collection<? extends GrantedAuthority> extractAuthorities(
            Authentication authentication) {
        return authentication.getAuthorities();
    }

    /**
     * 表示此AccessDecisionManager是否能够处理传递的ConfigAttribute呈现的授权请求
     */
    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }
    /**
     * 表示当前AccessDecisionManager实现是否能够为指定的安全对象（方法调用或Web请求）提供访问控制决策
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }
}
