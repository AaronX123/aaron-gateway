package gateway.common;

/**
 * @author xiaoyouming
 * @version 1.0
 * @since 2020-04-14
 */
public enum GatewayError {
    TOKEN_NOT_EXIST("050001","TOKEN已过期,请重新登录"),
    LOGIN_IN_OTHER_PLACE("050002","在别处登录"),
    NO_AUTHORITY("050003","无访问权限"),
    ;
    private String msg;
    private String code;

    GatewayError(String msg, String code) {
        this.msg = msg;
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
