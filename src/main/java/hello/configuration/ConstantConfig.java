package hello.configuration;

import lombok.Getter;

/**
 * 系统常量配置
 */
public class ConstantConfig {
    // TODO 定义常规变量
    /**
     * 时区
     */
    public static final String TIMEZONE = "GMT+8";

    /**
     * 时间格式
     */
    public static final String DATE_FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 时间格式-ZH
     */
    public static final String DATE_FORMAT_ZH = "yyyy年MM月dd日 HH:mm:ss";

    @Getter
    public enum WEB_URL {
        /**
         * 登录
         */
        LOGIN("/login", "POST"),

        LOGOUT("/logout"),

        LOGOUT_REDIRECT("/auth/logout");

        private String url;
        private String method;

        WEB_URL(String url, String method) {
            this.url = url;
            this.method = method;
        }


        WEB_URL(String url) {
            this.url = url;
            this.method = "GET";
        }
    }


}
