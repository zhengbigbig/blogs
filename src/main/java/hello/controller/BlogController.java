package hello.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import hello.entity.Blog;
import hello.entity.result.BlogResult;
import hello.entity.user.User;
import hello.service.impl.AuthServiceImpl;
import hello.service.impl.BlogServiceImpl;
import hello.utils.AssertUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.Map;

@Controller
public class BlogController {
    private final AuthServiceImpl authServiceImpl;
    private final BlogServiceImpl blogService;

    @Inject
    public BlogController(AuthServiceImpl authServiceImpl, BlogServiceImpl blogService) {
        this.authServiceImpl = authServiceImpl;
        this.blogService = blogService;
    }

    @GetMapping("/blog")
    @ResponseBody
    public IPage<Blog> getBlogs(@RequestParam("page") Integer page,
                                @RequestParam("pageSize") Integer pageSize,
                                @RequestParam(value = "userId", required = false) Integer userId) {
        return blogService.selectBlogListByUserOrAnonymous(page, pageSize, userId);
    }

    @GetMapping("/blog/{blogId}")
    @ResponseBody
    public Blog getBlog(@PathVariable("blogId") Long blogId) {
        return blogService.selectBlogById(blogId);
    }

    @PostMapping("/blog")
    @ResponseBody
    public Object newBlog(@RequestBody Blog blog) {
        try {
            return authServiceImpl.getCurrentUser()
                    .map(user -> blogService.saveOrUpdateBlog(blog, user.getId()))
                    .map(blog1 -> BlogResult.success("保存成功", blog))
                    .orElse(BlogResult.failure("登录后才能操作"));
        } catch (IllegalArgumentException e) {
            return BlogResult.failure(e);
        }
    }

    @PatchMapping("/blog/{blogId}")
    @ResponseBody
    public BlogResult updateBlog(@PathVariable("blogId") int blogId, @RequestBody Map<String, String> param) {
        try {
            return authServiceImpl.getCurrentUser()
                    .map(user -> blogService.saveOrUpdateBlog(fromParam(param, user), user.getId()))
                    .map(blog -> BlogResult.success("获取成功", blog))
                    .orElse(BlogResult.failure("登录后才能操作"));
        } catch (IllegalArgumentException e) {
            return BlogResult.failure(e);
        }
    }

    @DeleteMapping("/blog/{blogId}")
    @ResponseBody
    public Object deleteBlog(@PathVariable("blogId") Long blogId) {
        try {
            return authServiceImpl.getCurrentUser()
                    .map(user -> blogService.deleteBlog(blogId, user.getId()));
        } catch (IllegalArgumentException e) {
            return BlogResult.failure(e);
        }
    }

    private Blog fromParam(Map<String, String> params, User user) {
        Blog Blog = new Blog();
        String title = params.get("title");
        String content = params.get("content");
        String description = params.get("description");

        AssertUtils.assertTrue(StringUtils.isNotBlank(title) && title.length() < 100, "title is invalid!");
        AssertUtils.assertTrue(StringUtils.isNotBlank(content) && content.length() < 10000, "content is invalid");

        if (StringUtils.isBlank(description)) {
            description = content.substring(0, Math.min(content.length(), 10)) + "...";
        }

        Blog.setTitle(title);
        Blog.setContent(content);
        Blog.setDescription(description);
        Blog.setUser(user);
        return Blog;
    }
}
