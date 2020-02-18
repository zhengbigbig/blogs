package hello.entity;

import hello.entity.user.User;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class Blog {
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
