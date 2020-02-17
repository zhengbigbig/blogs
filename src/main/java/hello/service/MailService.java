package hello.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.mail.internet.MimeMessage;
import java.util.Map;

/**
 * 邮件发送工具类
 *
 * @create ZhengBigbig by 2020-02-16
 */

@Service
public class MailService {

    /**
     * 发送邮件的方法
     *
     * @param to 收件人邮箱地址
     * @param from 发件人邮箱地址
     */
    private JavaMailSender javaMailSender;
    private String TEXT = "  <div>\n" +
            "      <hr>\n" +
            "  <div style=\"color:#4D4D4D;font-size:15px;margin:20px\">\n" +
            "      <div style=\"font-weight:500;margin:20px 0;\">尊敬的用户：<span style=\"color:#ff6600;font-size:20px;margin:0 5px;text-decoration\">%s</span>，您好！</div>\n" +
            "  <div style=\"font-weight:500\">本次操作验证码为：<span style=\"color:red;font-size:20px\">%d</span></div>\n" +
            "    <div style=\"font-size:12px;margin:20px 0;color:#747474\">\n" +
            "      注意：此操作可能会修改您的密码。如非本人操作，请及时登录并修改密码以保证帐户安全!\n" +
            "    </div>\n" +
            "  </div>\n" +
            "  <hr>\n" +
            "  <div style=\"font-size:12px;margin:20px;color:#747474\">\n" +
            "    <p>此为系统邮件，请勿回复</p>\n" +
            "    <p>祝您生活愉快，学习进步</p>\n" +
            "  </div>\n" +
            "  </div>";

    @Inject
    public MailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendMail(Map<String, String> user, Integer code) throws Exception {
        MimeMessage msg = javaMailSender.createMimeMessage();

        // true = multipart message
        MimeMessageHelper helper = new MimeMessageHelper(msg, true);
        helper.setFrom("zhengbigbig@foxmail.com");
        // 设置收件人
        helper.setTo(user.get("email"));
        // 设置邮箱主题
        helper.setSubject("鸿博平台");

        // 设置邮箱正文
        // true = text/html
        String text = String.format(TEXT, user.get("username"), code);
        helper.setText(text, true);

        javaMailSender.send(msg);
    }

}
