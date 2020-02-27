package hello.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import hello.entity.Blog;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author zhengzhiheng
 * @since 2020-02-27
 */
public interface BlogMapper extends BaseMapper<Blog> {

    IPage<Blog> selectBlogListByUserOrAnonymous(IPage<Blog> page, @Param("userId") Integer userId);

    Blog selectBlogById(@Param("blogId") Long blogId);
}
