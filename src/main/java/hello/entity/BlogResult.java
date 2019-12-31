package hello.entity;

import java.util.List;

public class BlogResult extends Result<List<Blog>> {
    private int total;
    private int page;
    private int pageCount;

    public static BlogResult newResults(List<Blog> data, int total, int page, int pageCount) {
        return new BlogResult("success", "获取成功", data, total, page, pageCount);
    }

    public static BlogResult failure(String msg) {
        return new BlogResult("fail", msg, null, 0, 0, 0);
    }

    public BlogResult(String status, String msg, List<Blog> data, int total, int page, int pageCount) {
        super(status, msg, data);
        this.total = total;
        this.page = page;
        this.pageCount = pageCount;
    }

    public int getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public int getPageCount() {
        return pageCount;
    }
}
