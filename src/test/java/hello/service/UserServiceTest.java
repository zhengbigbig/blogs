package hello.service;


import hello.dao.UserMapper;
import hello.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    BCryptPasswordEncoder mockEncoder;
    @Mock
    UserMapper mockMapper;
    @InjectMocks
    UserService userService;

    @Test
    void testSave() {
        // 调用userService
        // 验证userService将请求转发给了userMapper
        when(mockEncoder.encode("myPwd")).thenReturn("encodedPwd");
        userService.save("myUser", "myPwd", "1@qq.com");
        verify(mockMapper).save("myUser", "encodedPwd", "1@qq.com");
    }

    @Test
    public void testFindUserByUsername() {
        // 行为：调用userService中方法，然后验证mockMapper方法中接受到的参数是否一致
        userService.getUserByUsername("myUser");
        verify(mockMapper).findUserByUsernameOrEmail("myUser");
    }

    @Test
    public void throwExceptionWhenUserNotFound() {
        when(mockMapper.findUserByUsernameOrEmail("myUser")).thenReturn(null);
        // 上面这句话是多余的，实际上不配置都会默认返回null

        Assertions.assertThrows(UsernameNotFoundException.class,
                () -> {
                    userService.loadUserByUsername("myUser");
                });
    }

    @Test
    public void returnUserDetailsWhenUserFound() {
        when(mockMapper.findUserByUsernameOrEmail("myUser"))
                .thenReturn(new User(1, "myUser", "encodedPwd"));
        UserDetails userDetails = userService.loadUserByUsername("myUser");
        Assertions.assertEquals("myUser", userDetails.getUsername());
        Assertions.assertEquals("encodedPwd", userDetails.getPassword());

    }
}