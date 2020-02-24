package hello.entity.result;

import hello.entity.Mail;

import java.io.Serializable;

public class MailResult extends Result<Object> implements Serializable {
    private static final long serialVersionUID = -3000523334436884063L;

    protected MailResult(ResultStatus status, String msg, Object data) {
        super(status, msg, data);
    }

    public static MailResult success(String msg, Mail mail) {
        return new MailResult(ResultStatus.OK, msg, mail);
    }

}
