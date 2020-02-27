package hello.entity.user;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import hello.configuration.ConstantConfig;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Data
@TableName("user")
public class User implements Serializable, UserDetails {

    private static final long serialVersionUID = -7571859707880852198L;
    private Long id;
    private String username;
    @JsonIgnore
    private String encryptedPassword;
    @TableField(exist = false)
    @JsonIgnore
    private String password;
    private String avatar;
    private String email;
    private Integer sex; // 0 无性别  1 男  2 女
    private String summary; //简介
    private String profession;
    private String address;
    private String technologyStack;
    @JsonFormat(pattern = ConstantConfig.DATE_FORMAT_ZH, timezone = ConstantConfig.TIMEZONE)
    @TableField(fill = FieldFill.INSERT)
    private Instant createdAt;
    @JsonFormat(pattern = ConstantConfig.DATE_FORMAT_ZH, timezone = ConstantConfig.TIMEZONE)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
    @TableField(exist = false)
    private List<Role> roles;
    @TableField(exist = false)
    @JsonIgnore
    private List<? extends GrantedAuthority> authorities;

    public User() {
    }

    public User(Long id, String username, String encryptedPassword) {
        this.id = id;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.avatar = "";
    }

    public User(Long id, String username, String encryptedPassword, String email) {
        if (id != null) {
            this.id = id;
        }
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.avatar = "";
        this.email = email;
    }

    public static User create(Long id, String username, String encryptedPassword, String email) {
        return new User(id, username, encryptedPassword, email);
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return encryptedPassword;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // 账号是否过期
    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 账号是否锁定
    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 密码是否过期
    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 账号是否可用
    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    // 这里equals 和 hashcode必须重写，因为后续存在registry中的会对比
    @Override
    public boolean equals(Object o) {
        if (o instanceof User) {
            return username.equals(((User) o).getUsername());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

}
