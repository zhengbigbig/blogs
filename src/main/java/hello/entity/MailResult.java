package hello.entity;

public class MailResult extends Result<Object> {
    protected MailResult(ResultStatus status, String msg, Object data) {
        super(status, msg, data);
    }

    public static MailResult success(String msg) {
        return new MailResult(ResultStatus.OK, msg, null);
    }


    public static MailResult failure(String msg) {
        return new MailResult(ResultStatus.FAIL, msg, null);
    }


}
