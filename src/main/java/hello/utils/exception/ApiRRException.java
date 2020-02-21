package hello.utils.exception;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiRRException extends RuntimeException {
    private static final long serialVersionUID = -6977011323190967375L;
    private String msg;
    private int error = 500;

    public ApiRRException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public ApiRRException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public ApiRRException(String msg, int error) {
        super(msg);
        this.msg = msg;
        this.error = error;
    }

    public ApiRRException(String msg, int error, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.error = error;
    }

}
