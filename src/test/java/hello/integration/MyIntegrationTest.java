package hello.integration;


import hello.Application;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.io.IOException;

@ExtendWith(SpringExtension.class)
// 对springboot的测试，环境变量端口应该是随机的，以免占用
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// 引入spring的配置
@TestPropertySource(locations = "classpath:test.properties")
public class MyIntegrationTest {
    @Inject
    Environment environment;

    @Test
    public void indexHtmlIsAccessible() throws IOException {
        String port = environment.getProperty("local.server.port");
        System.out.println(port);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://localhost:" + port + "/auth");

        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            if (response1.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity1 = response1.getEntity();
                byte[] bytes = EntityUtils.toByteArray(entity1);
                String s = new String(bytes);
                System.out.println("s = " + s);
                Assertions.assertEquals(200, response1.getStatusLine().getStatusCode());
                Assertions.assertTrue(s.contains("用户没有登录"));
                EntityUtils.consume(entity1);
            }
        }

    }
}
