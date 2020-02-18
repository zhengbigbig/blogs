package hello.entity.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Permission {
    private Integer id;
    private String name;
    private String description;
    // 授权链接，通配符
    private String url;
    // 父节点id
    private Integer pid;
}
