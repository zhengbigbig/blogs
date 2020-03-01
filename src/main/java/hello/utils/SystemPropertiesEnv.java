package hello.utils;

import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Created by zhengzhiheng on 2020/3/1 9:20 下午
 * Description:
 */
@Log
@Configuration
/**
 * 配置类必须以类的形式提供（不能是工厂方法返回的实例），允许通过生成子类在运行时增强（cglib 动态代理）Bean也一样 都只会是一个。
 * @Component 的类每次引入都会生成一个新的实例
 */
//@ComponentScan("hello")
//@ConfigurationProperties(prefix = "web-env")
@PropertySource(name = "systemEnv",value = "classpath:system.properties")
public class SystemPropertiesEnv {

    @Value("${web-env.webIgnore}")
    private String webIgnore;
    @Value("${web-env.securityPermit}")
    private String securityPermit;

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

    public void setWebIgnore(String webIgnore) {
        this.webIgnore = webIgnore;
    }

    public String[] getSecurityPermit() {
        log.info(securityPermit);
        if (StringUtils.isBlank(securityPermit)) {
            return null;
        } else {
            return securityPermit.split(",");
        }
    }

    public void setSecurityPermit(String securityPermit) {
        this.securityPermit = securityPermit;
    }
}
