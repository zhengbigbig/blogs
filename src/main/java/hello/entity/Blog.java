package hello.entity;

import hello.entity.user.User;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

@Data
public class Blog implements Serializable {
    private static final long serialVersionUID = -7617553288477666080L;
    private Integer id;
    private User user;
    private String title;
    private String description;
    private String content;
    private Instant updatedAt;
    private Instant createdAt;

    public Integer getUserId() {
        return user == null ? null : user.getId();
    }
}
