package hello.entity.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public class User implements UserDetails {

    private static final long serialVersionUID = -7571859707880852198L;
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
    @SuppressFBWarnings("SE_BAD_FIELD")
    private List<Role> roles;
    @JsonIgnore
    private List<? extends GrantedAuthority> authorities;

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

    public User(String username, String encryptedPassword, String avatar, String email, Integer sex, String summary, String profession, String address, String technologyStack, Instant createdAt, Instant updatedAt, List<Role> roles, List<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.avatar = avatar;
        this.email = email;
        this.sex = sex;
        this.summary = summary;
        this.profession = profession;
        this.address = address;
        this.technologyStack = technologyStack;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.roles = roles;
        this.authorities = authorities;
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

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public Integer getId() {
        return id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTechnologyStack() {
        return technologyStack;
    }

    public void setTechnologyStack(String technologyStack) {
        this.technologyStack = technologyStack;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}
