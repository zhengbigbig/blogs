package hello.entity.result;

import hello.entity.Mail;

public class MailResult extends Result<Object> {
    protected MailResult(ResultStatus status, String msg, Object data) {
        super(status, msg, data);
    }

    public static MailResult success(String msg, Mail mail) {
        return new MailResult(ResultStatus.OK, msg, mail);
    }

}
