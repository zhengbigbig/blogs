package hello.utils;

import lombok.Setter;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import javax.inject.Inject;

/**
 * Created by zhengzhiheng on 2020/3/1 9:20 下午
 * Description:
 */
@Log
@Configuration
/**
 * 配置类必须以类的形式提供（不能是工厂方法返回的实例），允许通过生成子类在运行时增强（cglib 动态代理）与之搭配的Bean也一样 都只会是一个。
 * @Component 的类每次引入都会生成一个新的实例
 */
//@ComponentScan("hello")
//@ConfigurationProperties(prefix = "web-env")
@Setter
@PropertySource(name = "systemEnv", value = "classpath:system.properties")
public class SystemPropertiesEnv {

    @Inject
    Environment env;

    @Value("${web-env.webIgnore}")
    private String webIgnore;
    @Value("${web-env.securityPermit}")
    private String securityPermit;
    @Value("${web-env.cors}")
    private Boolean cors;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    public String[] getWebIgnore() {
        if (StringUtils.isBlank(webIgnore)) {
            return null;
        } else {
            return webIgnore.split(",");
        }
    }

    public String[] getSecurityPermit() {
        log.info(securityPermit);
        log.info(env.getProperty("web-env.webIgnore"));
        if (StringUtils.isBlank(securityPermit)) {
            return null;
        } else {
            return securityPermit.split(",");
        }
    }

    public Boolean getCors() {
        return cors;
    }
}
