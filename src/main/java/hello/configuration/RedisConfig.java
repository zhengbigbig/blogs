package hello.configuration;

import hello.configuration.security.listener.SessionListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

// 创建一个Servlet过滤器
@Configuration
@EnableRedisHttpSession
public class RedisConfig {


    private RedisTemplate<String, String> redisTemplateString;
    //
//    @Bean
//    public HttpSessionIdResolver httpSessionIdResolver() {
//        return HeaderHttpSessionIdResolver.xAuthToken();
//    }

    @Bean
    public JedisConnectionFactory redisConnectionFactory() {
        return new JedisConnectionFactory();

    }


    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new SessionListener();
    }

    // gitHub 跑ci时出错解决 禁用自动配置
    @Bean
    public static ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }

    // 自定义CookieSerializer
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("JSESSIONID");
        serializer.setCookiePath("/");
        /*
         该正则会将Cookie设置在父域zbb.cn中，
         如果有另一个相同父域的子域名www.zbb.cn也会识别这个Cookie，
         便可以很方便的实现<同父域下>的单点登录
         */
        serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
        return serializer;
    }

    @Bean
    public RedisTemplate<String, String> getRedisTemplateString() {
        redisTemplateString = new StringRedisTemplate(redisConnectionFactory());
        RedisSerializer<String> keySerializer = new StringRedisSerializer();
        redisTemplateString.setKeySerializer(keySerializer);
        RedisSerializer<String> valueSerializer = new StringRedisSerializer();
        redisTemplateString.setValueSerializer(valueSerializer);
        return redisTemplateString;
    }
}
