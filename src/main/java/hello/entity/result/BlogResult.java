package hello.entity.result;

import hello.entity.Blog;

import java.io.Serializable;

public class BlogResult extends Result<Blog> implements Serializable {
    private static final long serialVersionUID = 4793833916700181788L;

    protected BlogResult(ResultStatus status, String msg, Blog data) {
        super(status, msg, data);
    }

    public static BlogResult failure(String message) {
        return new BlogResult(ResultStatus.FAIL, message, null);
    }

    public static BlogResult failure(Exception e) {
        return new BlogResult(ResultStatus.FAIL, e.getMessage(), null);
    }

    public static BlogResult success(String msg) {
        return new BlogResult(ResultStatus.OK, msg, null);
    }

    public static BlogResult success(String msg, Blog Blog) {
        return new BlogResult(ResultStatus.OK, msg, Blog);
    }
}
