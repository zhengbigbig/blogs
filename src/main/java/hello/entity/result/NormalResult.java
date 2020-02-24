package hello.entity.result;

import java.io.Serializable;

public class NormalResult extends Result implements Serializable {
    private static final long serialVersionUID = 6151695778498258614L;

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
