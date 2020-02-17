package hello.dao;

import hello.entity.User;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Map;

@Service
public class UserDao {
    private SqlSession sqlSession;

    @Inject
    public UserDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    public User findUserByUsernameOrEmail(String username) {
        return sqlSession.selectOne("getUserByUsernameOrEmail", username);
    }

    public void save(Map<String, String> parameter) {
        sqlSession.insert("insertUser", parameter);
    }

    public int updateUser(User user) {
        return sqlSession.update("updateUser", user);
    }
}
