package hello.entity.result;

public class ObjectResult extends Result<Object> {
    protected ObjectResult(ResultStatus status, String msg, Object data) {
        super(status, msg, data);
    }

    public static ObjectResult success(String msg, Object object) {
        return new ObjectResult(ResultStatus.OK, msg, object);
    }

    public static ObjectResult failure(String msg) {
        return new ObjectResult(ResultStatus.FAIL, msg, null);
    }
}
