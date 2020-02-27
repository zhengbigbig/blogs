package hello.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;

/**
 * <p>
 *
 * </p>
 *
 * @author zhengzhiheng
 * @since 2020-02-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class EmailSms implements Serializable {

    private static final long serialVersionUID = 5548896347008919713L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String email;

    private Integer sms;

    /**
     * 是否有效，1-有效，0-无效
     */
    @JsonIgnore
    private Integer usable;
    @JsonIgnore
    @TableField(fill = FieldFill.INSERT)
    private Instant createdAt;
    @JsonIgnore
    @TableField(fill = FieldFill.INSERT)
    private Instant deadLine;

    public EmailSms(String email, Integer sms) {
        this.email = email;
        this.sms = sms;
    }
}
