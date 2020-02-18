package hello.dao;

import hello.entity.user.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper {
    @Select("select * from sys_permission")
    List<Permission> findAllPermission();

    @Select("select sp.* from user u\n" +
            "    LEFT JOIN sys_role_user sru on u.id = sru.user_id\n" +
            "    LEFT JOIN sys_role sr on sru.role_id = sr.id\n" +
            "    LEFT JOIN sys_permission_role spr on sru.role_id = spr.role_id\n" +
            "    RIGHT JOIN sys_permission sp on spr.permission_id = sp.id\n" +
            "where u.id = #{userId}")
    List<Permission> findPermissionByUserId(@Param("userId") Integer userId);
}
