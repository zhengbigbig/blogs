package hello.dao;

import com.google.common.collect.ImmutableMap;
import hello.entity.Blog;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class BlogDao {
    private final SqlSession sqlSession;

    @Inject
    public BlogDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    public List<Blog> getBlogs(Integer page, Integer pageSize, Integer userId) {
        ImmutableMap<String, Object> parameters = ImmutableMap.of(
                "offset", (page - 1) * pageSize,
                "limit", pageSize,
                "user_id", userId
        );
        return sqlSession.selectList("BlogMapper.selectBlogByUserId", parameters);
    }

    public int count(Integer userId) {
        return 0;
    }
}
