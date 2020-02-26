package hello.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hello.mapper.UserMapper;
import hello.entity.user.User;
import hello.service.UserPlusService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserPlusService {

}
