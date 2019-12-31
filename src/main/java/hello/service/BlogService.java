package hello.service;

import hello.dao.BlogDao;
import hello.entity.Blog;
import hello.entity.BlogResult;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class BlogService {
    private BlogDao blogDao;

    @Inject
    public BlogService(BlogDao blogDao) {
        this.blogDao = blogDao;
    }

    public BlogResult getBlogs(Integer page, Integer pageSize, Integer userId) {
        try {
            List<Blog> blogs = blogDao.getBlogs(page, pageSize, userId);
            int total = blogDao.count(userId);
            int pageCount = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
            return BlogResult.newResults(blogs, total, page, pageCount);
        } catch (Exception e) {
            return BlogResult.failure("系统异常");
        }
    }
}
