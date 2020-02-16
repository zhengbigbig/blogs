package hello.entity;

import java.time.Instant;

/**
 * usable 是否有效，1-无效，2-有效,
 * send 是否已发送，1-未发送，2-已发送
 */

public class Mail {
    private Integer id;
    private Integer userId;
    private String email;
    private Integer sms;
    private Integer usable;
    private Integer send;
    private Instant createdAt;
    private Instant deadLine;

    public Mail(Integer userId, String email, Integer sms, Integer usable, Integer send) {
        this.userId = userId;
        this.email = email;
        this.sms = sms;
        this.usable = usable;
        this.send = send;
        this.createdAt = Instant.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public Integer getSend() {
        return send;
    }

    public void setSend(Integer send) {
        this.send = send;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
