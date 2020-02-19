package hello.entity.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class User {
    private Integer id;
    private String username;
    @JsonIgnore
    private String encryptedPassword;
    private String avatar;
    private String email;
    private Integer sex; // 0 无性别  1 男  2 女
    private String summary; //简介
    private String profession;
    private String address;
    private String technologyStack;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Instant createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Instant updatedAt;

    private List<Role> roles;

    public User() {
    }

    public User(Integer id, String username, String encryptedPassword) {
        this.id = id;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.avatar = "";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public User(Integer id, String username, String encryptedPassword, String email) {
        this.id = id;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.avatar = "";
        this.email = email;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
