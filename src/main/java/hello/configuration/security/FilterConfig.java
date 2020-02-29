//package hello.configuration.security;
//
//import hello.configuration.security.datasource.MyAccessDecisionManager;
//import hello.configuration.security.datasource.MyInvocationSecurityMetadataSourceService;
//import hello.mapper.PermissionMapper;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
//
//import javax.inject.Inject;
//
///**
// * Created by zhengzhiheng on 2020/2/29 3:03 下午
// * Description:
// */
//@Configuration
//public class FilterConfig {
//
//    /**
//     * 资源管理,决策放行
//     *
//     * @return FilterInvocationSecurityMetadataSource
//     */
//
//    @Inject private PermissionMapper permissionMapper;
//
//    @Bean
//    public FilterRegistrationBean filterSecurityInterceptor() {
//        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
//        FilterSecurityInterceptor interceptor = new FilterSecurityInterceptor();
////        {
////            @Override
////            public void init(FilterConfig arg0) {
////                System.out.println(arg0);
////            }
////        };
//        interceptor.setAccessDecisionManager(new MyAccessDecisionManager());
//        interceptor.setSecurityMetadataSource(new MyInvocationSecurityMetadataSourceService(permissionMapper));
//        registrationBean.setFilter(interceptor);
////        registrationBean.addUrlPatterns("*.json");//配置过滤规则
////        registrationBean.setUrlPatterns();
//        registrationBean.addInitParameter("excludeUrls", "hahahhhaa");//设置init参数
//        registrationBean.setName("securityFilter1");//设置过滤器名称
//        registrationBean.setOrder(0);//执行次序
//        return registrationBean;
//    }
//
//
//}
