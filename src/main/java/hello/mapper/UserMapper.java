package hello.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import hello.entity.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

//可以继承或者不继承BaseMapper
public interface UserMapper extends BaseMapper<User> {


    /**
     * 自定义分页查询
     *
     * @param userPage 单独 user 模块使用的分页
     * @return 分页数据
     */
//    UserPage selectUserPage(UserPage userPage);
//
//    List<User> findList(@Param("user") User user);

//    User getUserByUsernameOrEmail(@Param("ew") Wrapper ew);


    int updateUser(@Param("user") User user);

}
