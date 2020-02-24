package hello.entity.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class Role implements Serializable {
    private static final long serialVersionUID = 4853927272427836957L;
    private Integer id;
    private String name;
}
