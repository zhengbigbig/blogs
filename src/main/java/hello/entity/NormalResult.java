package hello.entity;

public class NormalResult extends Result {
    protected NormalResult(ResultStatus status, String msg, Object data) {
        super(status, msg, data);
    }

    public static NormalResult success(String msg) {
        return new NormalResult(ResultStatus.OK, msg, null);
    }

    public static NormalResult failure(String msg) {
        return new NormalResult(ResultStatus.FAIL, msg, null);
    }
}
