# 多人在线博客平台

[![Build Status](https://travis-ci.com/zhengbigbig/blogs.svg?branch=master)](https://travis-ci.com/zhengbigbig/blogs)

## spring-security-basic branch
- 掌握spring security自定义配置
- 熟悉并理解原理
- filter/handler/manager/provider等
- 中途踩了许多坑，最后终于配好了
1. 最开始自定义引入FilterSecurityInterceptor导致login/logout也被拦截掉，是由于没有配置好HttpSecurity
网上一些博客都漏洞百出，终于自己看源码debugger才知道，login/logout拦截器默认的url是/login /logout，都不需要设置permitAll，
最开始设置了/auth/login导致登录被拦截
2. 为遵循Restful把相应拦截器处理等全部重写，依旧有个问题待解决，因为antMatchers("/xxx").permitAll() 不起作用。
最后弄了很久才知道，HttpSecurity存在顺序问题，起初没找到，看网上说在 WebSecurity.ignore().antMatchers("/xxx")，
误人子弟啊！
ignore后，导致登录后，再发/xxx请求根本拿不到SecurityContextHolder上下文，最后无奈问大佬然后看源码，
才知道ignore后不走拦截器，第一个请求结束都被persistent Filter，保存到了Http Session并清理掉了，为了安全，
不走拦截器是拿不到上下文的。web ignore只针对那些不用走过滤器不考虑上下文的静态文件，web资源等。
3. 自定义FilterSecurityInterceptor， 在FilterInvocationSecurityMetadataSource加载权限资源，
在AccessDecisionManager决策是否放行，这里又一个坑，假若我想做动态权限管理呢，又恰好某些是我想放行的uri呢
把源码好好看了一遍，才知道，可以设置为ROLE_ANONYMOUS权限。
4. 解决办法的唯一办法是就是  debugger看源码了
5. 再说antMatchers("/xxx").permitAll()，并不是不走过滤器了，是将antMatchers内容全部加载到过滤器，
然后再靠后的FilterSecurityInterceptor过滤器时，匹配到才放行了，若是ROLE_ANONYMOUS也放行，你单单在
AccessDecisionManager中return是不会被放行的
6. 为了预留多项登录方式，重定义了provider AbstractAuthenticationProcessingFilter等，
自定义过滤器不要使用@Bean注入，会有bug

## spring-security-redis branch
1. 引入redis session对session进一步测试，未写完整，后面使用token，只记录思路
2. login后进行HttpSession进行session.setAttribute(session.getId(),response.getData())
3. logout后清除session.removeAttribute(session.getId());
4. 定义拦截器对session进行判断，有则放行，没有则拒绝
5. 根据实际业务实际实现，后面将使用 JWT+Redis

