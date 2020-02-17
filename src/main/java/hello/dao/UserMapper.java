package hello.dao;

import hello.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("select * from user where username = #{username} or email = #{username}")
    User findUserByUsernameOrEmail(@Param("username") String username);

    @Select("insert into user(username, encrypted_password, email, created_at, updated_at) " +
            "values(#{username}, #{encryptedPassword}, #{email}, now(), now())")
    void save(@Param("username") String username, @Param("encryptedPassword") String encryptedPassword, @Param("email") String email);



}