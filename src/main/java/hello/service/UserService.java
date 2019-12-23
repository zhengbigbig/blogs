package hello.service;

import hello.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// 之前使用WebSecurityConfig中的UserDetailsService是Mock的，现在来实现
@Service
public class UserService implements UserDetailsService {
    private Map<String, String> userPasswords = new ConcurrentHashMap<>();

    public UserService() {
        userPasswords.put("abc", "abc");
    }

    public void save(String username, String password) {
        userPasswords.put(username, password);
    }

    public String getPassword(String username) {
        return userPasswords.get(username);
    }

    public User getUserById(Integer id) {
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!userPasswords.containsKey(username)) {
            throw new UsernameNotFoundException(username + "不存在！");
        }
        String password = userPasswords.get(username);

        return new org.springframework.security.core.userdetails.User(username, password, Collections.emptyList());
    }
}
