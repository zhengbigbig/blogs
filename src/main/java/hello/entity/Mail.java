package hello.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * usable 是否有效，1-无效，2-有效,
 */

@Getter
@Setter
public class Mail {
    @JsonIgnore
    private Integer id;
    private String email;
    private Integer sms;
    @JsonIgnore
    private Integer usable;
    @JsonIgnore
    private Instant createdAt;
    @JsonIgnore
    private Instant deadLine;

    // Mybatis框架会调用这个默认构造方法来构造实例对象，即实体类需要通过Mybatis进行动态反射生成。
    public Mail() {
    }

    public Mail(String email, Integer sms) {
        this.email = email;
        this.sms = sms;
        this.usable = 1;
        this.createdAt = Instant.now();
        this.deadLine = Instant.now().plusSeconds(180);
    }
}
