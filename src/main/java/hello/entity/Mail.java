package hello.entity;

import java.time.Instant;

/**
 * usable 是否有效，1-无效，2-有效,
 */

public class Mail {
    private Integer id;
    private String email;
    private Integer sms;
    private Integer usable;
    private Instant createdAt;
    private Instant deadLine;

    public Mail( String email, Integer sms, Integer usable) {
        this.email = email;
        this.sms = sms;
        this.usable = usable;
        this.createdAt = Instant.now();
        this.deadLine  = Instant.now().plusSeconds(180);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
