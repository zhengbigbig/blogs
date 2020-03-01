# 多人在线博客平台

[![Build Status](https://travis-ci.com/zhengbigbig/Blogs.svg?branch=master)](https://travis-ci.com/zhengbigbig/Blogs)

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
7. 补充
- 在配置```FilterSecurityInterceptor```时会出现各种各样的问题，什么俩次拦截然后返回error的问题，
因此网上很多教程都是误人子弟，拦截器或者过滤器添加后并不会覆盖原有，具体可以去看```addFilter```四个方法，只是指定了filterChain的顺序，那要怎么做呢？
在```HttpSecurity```中并没有提供```FilterSecurityInterceptor```方法，因此，
需要增加一个扩展方法```withObjectPostProcessor```，来对原有```FilterSecurityInterceptor```进行修改
- 配置```FilterSecurityInterceptor```后会出现```.antMatchers("/auth/**").permitAll()```为什么还是被拦截了？
配置如下：
```java
    http
        .authorizeRequests()
        .antMatchers(securityUrlPermit).permitAll()
        .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
            @Override
            public <O extends FilterSecurityInterceptor> O postProcess(
                    O fsi) {
                fsi.setSecurityMetadataSource(mySecurityMetadataSource(fsi.getSecurityMetadataSource()));
                return fsi;
            }
        })
```
这时需要你```fsi.getSecurityMetadataSource()```拿出来然后传入自定义的```FilterInvocationSecurityMetadataSource```
- 传入之后，在自定义的```FilterInvocationSecurityMetadataSource```的```getAttributes```中，若是放行资源则返回```Collection<ConfigAttribute>```，
之后```debugger```源码可知，调用了```DefaultFilterInvocationSecurityMetadataSource```进行通配符匹配，匹配成功后放行
- 你可能想在数据库中定义，只需要将对应url，赋予```ROLE_ANONYMOUS```权限

## spring-security-redis branch
1. 引入redis session对session进一步测试，未写完整，后面使用token，只记录思路
2. login后进行HttpSession或SessionRepository进行sessionId和数据保存
3. logout后清除掉
4. 定义拦截器对session进行判断，有则放行，没有则拒绝
5. 根据实际业务实际实现，spring security实际上已经帮你做了，但需要更复杂的功能，可以使用各阶段的hooks进行定制
6. 解决travis-ci Error creating bean with name 'enableRedisKeyspaceNotificationsInitializer' 报错
增加配置如下， 禁用自动配置

```java
    @Bean
    public static ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }
```
7. 默认的SessionCreationPolicy：```IF_REQUIRED```，
会导致maximumSessions无效，用户异地依旧可以登录，设置成NEVER即可解决这个问题，
原理是避免spring security创建新的session，不经过
设置NEVER避免spring security创建新的session 
8. 分支最后更新到集成spring security session redis，可配合nginx分布式部署，但管理用户上下线功能未重构

## spring-security-jwt-redis 
1. 引入依赖HuTool对token操作进行封装，包含AES加密解密
2. 引入```mybatis convert to mybatis-plus```，并集成AutoGenerator自动生成代码，减少重复劳动
并自定义mybatis-plus handler
3. 集成```p6spy```，可在控制台查看mybatis日志，可通过日志进行优化分析
4. 使用```DaoAuthenticationProvider```替换之前自己写的provider，并增加token的provider
5. 注意mybatis-plus非静态，需要注意bean依赖关系
6. 对登录成功的用户，将token保存在redis中，现在策略是只保存一个，若需要其他，只需要重写逻辑，譬如不同设备允许登录一个
7. 登录之后访问带权限的接口时，进行token的校验，若校验通过，看是否token快过期，若快过期则刷新token有效时间，优化用户体验
8. 官方```AccessDecisionManager```规则有三：
- ```AffirmativeBased``` 一票通过 （默认）
- ```UnanimousBased``` 一票否决
- ```ConsensusBased``` 少数服从多数
这里定义bean
```java
    @Bean
    public AccessDecisionManager accessDecisionManager() {
        List<AccessDecisionVoter<? extends Object>> decisionVoters
                = Arrays.asList(
                new WebExpressionVoter(),
                new AuthenticatedVoter(),
                new RoleBasedVoter()
        );
        return new AffirmativeBased(decisionVoters);
    }
```
例如访问/auth接口，设置了permitAll，投票会如下
```WebExpressionVoter```投票的是ExpressionUrlAuthorizationConfigurer中定义的权限，会通过
```AuthenticatedVoter``` 权限有三：```IS_AUTHENTICATED_FULLY IS_AUTHENTICATED_REMEMBERED IS_AUTHENTICATED_ANONYMOUSLY```，有一则进行深度校验，具体可看源码
这里未设置，则会弃权 
自定义中```RoleBasedVoter```，因为访问的```/auth```是permitAll，没有定义权限，则会直接弃权，当然，可以自定义
这里我们使用 ```UnanimousBased```，也就是一票否决，俩个弃权，一个通过，结果为通过
