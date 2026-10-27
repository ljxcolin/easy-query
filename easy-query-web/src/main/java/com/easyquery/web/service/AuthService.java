package com.easyquery.web.service;

import com.easyquery.web.model.LoginRequest;
import com.easyquery.web.model.LoginResponse;

/**
 * 认证服务接口：手机号 + 验证码登录注册合一
 *
 * @author easy-query
 */
public interface AuthService {

    /**
     * 发送短信验证码（模拟实现）
     *
     * @param phone 手机号
     * @return 验证码（仅供联调阶段前端自动填充，接入真实短信后必须移除）
     */
    String sendCode(String phone);

    /**
     * 登录/注册：用户不存在时自动注册，存在则直接登录
     *
     * @param request 登录请求（手机号 + 验证码）
     * @return 登录响应（含 JWT token）
     */
    LoginResponse login(LoginRequest request);
}
