package hello.dao;

import hello.entity.Blog;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlogDao {
    public List<Blog> getBlogs(Integer page, Integer pageCount, Integer userId) {
        return null;
    }

    public int count(Integer userId) {
        return 0;
    }
}
