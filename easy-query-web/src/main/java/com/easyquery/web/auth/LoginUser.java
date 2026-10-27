package com.easyquery.web.auth;

/**
 * 登录用户信息（由 JWT 解析得出）
 *
 * @param userId 用户 ID
 * @param phone  手机号
 * @author easy-query
 */
public record LoginUser(Long userId, String phone) {
}
