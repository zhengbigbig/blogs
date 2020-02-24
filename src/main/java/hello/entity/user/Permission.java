package hello.entity.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class Permission implements Serializable {
    private static final long serialVersionUID = -2034540928365956027L;
    private Integer id;
    private String name;
    private String description;
    // 授权链接，通配符
    private String url;
    // 父节点id
    private Integer pid;
}
