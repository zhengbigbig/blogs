package hello.configuration.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("start insert fill ....");
        Instant now = Instant.now();
        // global
        this.setFieldValByName("createdAt", now, metaObject);
        this.setFieldValByName("updatedAt", now, metaObject);
        // email
        this.setFieldValByName("deadLine", now.plusSeconds(180), metaObject);
    }


    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("start update fill ....");
        Instant now = Instant.now();
        // global
        this.setFieldValByName("updatedAt", now, metaObject);
    }
}