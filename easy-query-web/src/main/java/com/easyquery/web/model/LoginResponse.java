package com.easyquery.web.model;

/**
 * 登录/注册响应
 *
 * @author easy-query
 */
public class LoginResponse {

    /** JWT token */
    private String token;

    /** 用户 ID */
    private Long userId;

    /** 手机号 */
    private String phone;

    /** 昵称 */
    private String nickname;

    /** 是否首次注册 */
    private boolean newUser;

    public LoginResponse() {
    }

    public LoginResponse(String token, Long userId, String phone, String nickname, boolean newUser) {
        this.token = token;
        this.userId = userId;
        this.phone = phone;
        this.nickname = nickname;
        this.newUser = newUser;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isNewUser() {
        return newUser;
    }

    public void setNewUser(boolean newUser) {
        this.newUser = newUser;
    }
}
