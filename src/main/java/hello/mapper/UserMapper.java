package hello.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import hello.entity.user.User;
import org.apache.ibatis.annotations.Param;

//可以继承或者不继承BaseMapper
public interface UserMapper extends BaseMapper<User> {
    /**
     * 如果自定义的方法还希望能够使用MP提供的Wrapper条件构造器，则需要如下写法
     *
     * @param userWrapper new QueryWrapper<>()
     * @return User
     */
    User getUserByUsernameOrEmail(@Param(Constants.WRAPPER) Wrapper<User> userWrapper);


    int updateUser(@Param("user") User user);

    int deleteOne();

}
