package hello.entity.result;

import hello.entity.Blog;
import lombok.Value;

import java.io.Serializable;
import java.util.List;

@Value
public class BlogListResult extends Result<List<Blog>> implements Serializable {
    private static final long serialVersionUID = -4396597443716014666L;

    public static BlogListResult success(List<Blog> data) {
        return new BlogListResult(ResultStatus.OK, "获取成功", data);
    }

    public static BlogListResult failure(String msg) {
        return new BlogListResult(ResultStatus.FAIL, msg, null);
    }

    private BlogListResult(ResultStatus status, String msg, List<Blog> data) {
        super(status, msg, data);
    }
}
