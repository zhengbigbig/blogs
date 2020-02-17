package hello.dao;

import hello.entity.Mail;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class MailDao {
    private final SqlSession sqlSession;

    @Inject
    public MailDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    public Mail getSmsByEmail(String email) {
        return sqlSession.selectOne("selectValidSms", email);
    }

    public Mail insertSms(Mail mail) {
        sqlSession.insert("insertSms", mail);
        return getSmsByEmail(mail.getEmail());
    }

    public Mail updateSms(String email) {
        sqlSession.update("updateSms", email);
        return getSmsByEmail(email);
    }
}
