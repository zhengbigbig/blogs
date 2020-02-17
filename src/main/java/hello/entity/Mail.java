package hello.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

/**
 * usable 是否有效，1-无效，2-有效,
 */

public class Mail {
    private Integer id;
    private String email;
    private Integer sms;
    private Integer usable;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Instant createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getSms() {
        return sms;
    }

    public void setSms(Integer sms) {
        this.sms = sms;
    }

    public Integer getUsable() {
        return usable;
    }

    public void setUsable(Integer usable) {
        this.usable = usable;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getDeadLine() {
        return deadLine;
    }

    public void setDeadLine(Instant deadLine) {
        this.deadLine = deadLine;
    }
}
