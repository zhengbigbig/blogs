package hello.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import hello.mapper.UserMapper;
import hello.entity.user.User;
import hello.service.UserPlusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserMapper userMapper;

    @Autowired
    UserPlusService userPlusService;

    @GetMapping("/getAll")
    public List<User> getAll() {
        return userMapper.selectList(null);
    }

    @PostMapping("/insertUser")
    public Integer insertUser(@RequestBody User user) {
        return userMapper.insert(user);
    }

    @PostMapping("/updateUser")
    public Integer updateUser(@RequestBody User user) {
        return userMapper.updateById(user);
    }

    @DeleteMapping("/deleteUser/{id}")
    public Integer deleteUser(@PathVariable("id") String id) {
        return userMapper.deleteById(id);
    }

    @GetMapping("/getUser")
    public List<User> getUser() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper
                .isNotNull("nick_name")
                .ge("age", 18);
        return userMapper.selectList(queryWrapper);
    }

    @GetMapping("/findPage")
    public IPage<User> findPage() {
        Page<User> page = new Page<>(1, 5);

        return userMapper.selectPage(page, null);
    }

    @GetMapping("/getAllByService")
    public List<User> getAllByService() {
        return userPlusService.list();
    }

    @PostMapping("/insertUserByService")
    public Boolean insertUserByService(@RequestBody User user) {
        return userPlusService.save(user);
    }

    @PostMapping("/updateUserByService")
    public Boolean updateUserByService(@RequestBody User user) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("id", user.getId());

        return userPlusService.update(user, queryWrapper);
    }

    @DeleteMapping("/deleteUserByService/{id}")
    public Boolean deleteUserByService(@PathVariable("id") String id) {
        return userPlusService.removeById(id);
    }
}
