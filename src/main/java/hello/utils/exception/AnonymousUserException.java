package hello.utils.exception;

public class AnonymousUserException extends RuntimeException {
    private static final long serialVersionUID = -1162970914509715412L;
    private String msg;
    private int error = 500;

    public AnonymousUserException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public AnonymousUserException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public AnonymousUserException(String msg, int error) {
        super(msg);
        this.msg = msg;
        this.error = error;
    }

    public AnonymousUserException(String msg, int error, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.error = error;
    }
}
