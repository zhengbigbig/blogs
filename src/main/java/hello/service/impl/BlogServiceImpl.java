package hello.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hello.entity.Blog;
import hello.mapper.BlogMapper;
import hello.service.BlogService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhengzhiheng
 * @since 2020-02-27
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {
    @Inject
    private BlogMapper blogMapper;
    @Inject
    private BlogService blogService;

    public IPage<Blog> selectBlogListByUserOrAnonymous(Integer page, Integer pageSize, Integer userId) {
        Page<Blog> iPage = new Page<>(page > 0 ? page : 1, pageSize > 0 ? pageSize : 10);
        return blogMapper.selectBlogListByUserOrAnonymous(iPage, userId);
    }

    public Blog saveOrUpdateBlog(Blog blog, Long userId) {
        Blog blogResult = selectBlogById(blog.getId());
        if (blogResult == null) {
            return null;
        }
        if (!userId.equals(blogResult.getUserId())) {
            return null;
        }
        // 这里还需要判断用户权限

        boolean isSuccess = blogService.saveOrUpdate(blog);
        if (isSuccess) {
            return blogResult;
        }
        return null;
    }

    public Blog selectBlogById(Long blogId) {
        return blogMapper.selectBlogById(blogId);
    }

    public int deleteBlog(Long blogId, Long userId) {
        // 这里还需要判断用户权限
        Blog blogResult = selectBlogById(blogId);
        if (blogResult == null) {
            return -1;
        }
        if (blogResult.getUserId().equals(userId)) {
            return -2;
        }
        return blogMapper.deleteById(blogId);

    }
}
