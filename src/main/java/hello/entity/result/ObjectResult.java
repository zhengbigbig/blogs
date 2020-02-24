package hello.entity.result;

import java.io.Serializable;

public class ObjectResult extends Result<Object> implements Serializable {
    private static final long serialVersionUID = 1293346174830242143L;

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
