package hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import hello.configuration.security.facade.AuthenticationFacade;
import hello.service.AuthService;
import hello.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpSession;
import java.nio.charset.Charset;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
        // 使用springboot test 来模拟http请求
class AuthControllerTest {
    private MockMvc mvc;
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Mock
    private UserService userService;
    @Mock
    private AuthenticationFacade authenticationFacade;

    // 对每个测试构建
    @BeforeEach
    void setUp() {

        AuthService authService = new AuthService(userService, authenticationFacade);
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(userService, authService)).build();
    }

    //
    @Test
    void returnNotLoginByDefault() throws Exception {
        mvc.perform(get("/auth/currentUser")).andExpect(status().isOk())
                .andExpect(mvcResult -> Assertions.assertTrue(mvcResult.getResponse().getContentAsString(Charset.defaultCharset()).contains("用户没有登录")));
    }

    @Test
    void testLogin() throws Exception {
        /*
         * 未登录时，/auth接口返回未登录状态
         */
        mvc.perform(get("/auth/currentUser")).andExpect(status().isOk())
                .andExpect(mvcResult -> Assertions.assertTrue(mvcResult.getResponse().getContentAsString(Charset.defaultCharset()).contains("用户没有登录")));

        /*
         * 使用/auth/login登录
         * Mock登录参数，mock userService的返回，避免空指针异常
         * 登录之后，获取session，然后再次查看/auth的返回
         * expect 应该是已登录
         */

        // 使用guava 不可变map来操作，简化
        ImmutableMap<String, String> usernamePwd = ImmutableMap.of("username", "myUser", "password", "myPwd");
        // 避免空指针异常，使用mock的值
        when(userService.loadUserByUsername("myUser")).thenReturn(new User("myUser", bCryptPasswordEncoder.encode("myPwd"), Collections.emptyList()));
        when(userService.getUserByUsernameOrEmail("myUser")).thenReturn(new hello.entity.user.User(1, "myUser", bCryptPasswordEncoder.encode("myPwd")));

        String s = new ObjectMapper().writeValueAsString(usernamePwd); // json
        MvcResult response = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(s))
                .andExpect(status().isOk())
                .andExpect(result -> Assertions.assertTrue(result.getResponse().getContentAsString(Charset.defaultCharset()).contains("登录成功")))
                .andReturn();
        // 这里是拿不到cookie的，因为这里只是对controller的单测，实际会涉及到鉴权之类，这里只验证session
        HttpSession session = response.getRequest().getSession();

        assert session != null;
        mvc.perform(get("/auth/currentUser").session((MockHttpSession) session)).andExpect(status().isOk())
                .andExpect(mvcResult -> Assertions.assertTrue(mvcResult.getResponse().getContentAsString(Charset.defaultCharset()).contains("myUser")));
    }

    /*
     * 测试注册接口
     */
    @Test
    void testRegister() throws Exception {
        ImmutableMap<String, String> usernamePwd = ImmutableMap.of(
                "username", "用户名_a1", "password", "zA_.-a12",
                "email", "1@qq.com", "sms", "111111"
        );
        ImmutableMap<String, String> errorUsername = ImmutableMap.of(
                "username", "1", "password", "myPwd"
        );
        ImmutableMap<String, String> errorPwd = ImmutableMap.of(
                "username", "myUser", "password", "12345"
        );
        when(userService.isEqualSms("1@qq.com", 111111)).thenReturn(true);
        testRegisterValidateReturn(usernamePwd, "注册成功");
        testRegisterValidateReturn(errorUsername, "invalid username");
        testRegisterValidateReturn(errorPwd, "invalid password");

    }

    private void testRegisterValidateReturn(ImmutableMap<String, String> usernamePwd, String expectation) throws Exception {
        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(new ObjectMapper().writeValueAsString(usernamePwd)))
                .andExpect(status().isOk())
                .andExpect(result -> Assertions.assertTrue(result.getResponse().getContentAsString(Charset.defaultCharset()).contains(expectation)));
    }

}