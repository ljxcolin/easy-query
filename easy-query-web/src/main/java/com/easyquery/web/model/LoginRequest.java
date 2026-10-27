package com.easyquery.web.model;

/**
 * 登录/注册请求（手机号 + 短信验证码，登录注册合一）
 *
 * @author easy-query
 */
public class LoginRequest {

    /** 手机号 */
    private String phone;

    /** 短信验证码 */
    private String code;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
