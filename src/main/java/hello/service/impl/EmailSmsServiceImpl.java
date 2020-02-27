package hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hello.entity.EmailSms;
import hello.entity.result.MailResult;
import hello.entity.result.NormalResult;
import hello.entity.result.Result;
import hello.mapper.EmailSmsMapper;
import hello.service.EmailSmsService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhengzhiheng
 * @since 2020-02-27
 */
@Service
public class EmailSmsServiceImpl extends ServiceImpl<EmailSmsMapper, EmailSms> implements EmailSmsService {
    @Inject
    private EmailSmsMapper emailSmsMapper;
    @Inject
    private EmailSmsService emailSmsService;

    // E
    /**
     * 发送邮件的方法
     *
     * @param to 收件人邮箱地址
     * @param from 发件人邮箱地址
     */
    @Inject
    private JavaMailSender javaMailSender;

    private String TEXT = "  <div>%n" +
            "      <hr>%n" +
            "  <div style=\"color:#4D4D4D;font-size:15px;margin:20px\">%n" +
            "      <div style=\"font-weight:500;margin:20px 0;\">尊敬的用户：<span style=\"color:#ff6600;font-size:20px;margin:0 5px;text-decoration\">%s</span>，您好！</div>%n" +
            "  <div style=\"font-weight:500\">本次操作验证码为：<span style=\"color:red;font-size:20px\">%d</span></div>%n" +
            "    <div style=\"font-size:12px;margin:20px 0;color:#747474\">%n" +
            "      注意：此操作可能会修改您的密码。如非本人操作，请及时登录并修改密码以保证帐户安全!%n" +
            "    </div>%n" +
            "  </div>%n" +
            "  <hr>%n" +
            "  <div style=\"font-size:12px;margin:20px;color:#747474\">%n" +
            "    <p>此为系统邮件，请勿回复</p>%n" +
            "    <p>祝您生活愉快，学习进步</p>%n" +
            "  </div>%n" +
            "  </div>";

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


    // S

    // 生成6位验证码并发送邮件，发送后存入数据库
    public Result sendMailIfSuccessThenSaveSms(Map<String, String> registerUser) {
        String email = registerUser.get("email");
        Integer code = new Random().nextInt(999999);
        try {
            sendMail(registerUser, code);
            EmailSms emailSms = new EmailSms(email, code);
            insertEmailSms(emailSms);
            return MailResult.success("获取成功", emailSms);
        } catch (Exception e) {
            e.printStackTrace();
            return NormalResult.failure("邮件发送异常");
        }

    }

    public int getSmsByEmail(String email) {
        return Optional.ofNullable(selectValidEmailSms(email))
                .map(EmailSms::getSms)
                .orElse(-1);
    }

    public boolean isEqualSms(String email, Integer fromRequest) {
        Integer sms = getSmsByEmail(email);
        return sms != -1 && sms.equals(fromRequest);
    }


    // C
    public int insertEmailSms(EmailSms emailSms) {
        return emailSmsMapper.insert(emailSms);
    }

    // U
    public boolean invalidEmailSms(String email) {
        UpdateWrapper<EmailSms> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("usable", 0).eq("email", email);
        return emailSmsService.update(updateWrapper);
    }

    // R
    public EmailSms selectValidEmailSms(String email) {
        QueryWrapper<EmailSms> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email)
                .eq("usable", 1)
                .ge("dead_line", Instant.now())
                .orderByDesc("id")
                .apply("limit 0,1");
        return emailSmsMapper.selectOne(queryWrapper);
    }

    // D 数据库定时删除


}
