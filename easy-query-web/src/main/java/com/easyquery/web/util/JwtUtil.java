package com.easyquery.web.util;

import com.easyquery.web.auth.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类（HS256）
 *
 * @author easy-query
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey secretKey;

    private final long expireMillis;

    public JwtUtil(@Value("${easyquery.jwt.secret}") String secret,
                   @Value("${easyquery.jwt.expire-minutes:10080}") long expireMinutes) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMillis = expireMinutes * 60 * 1000;
    }

    /**
     * 生成 JWT
     *
     * @param userId 用户 ID
     * @param phone  手机号
     * @return token 字符串
     */
    public String generateToken(Long userId, String phone) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expireMillis);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("phone", phone)
                .issuedAt(now)
                .expiration(expire)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析 JWT，非法或过期 token 抛出 JwtException
     *
     * @param token token 字符串
     * @return 登录用户信息
     */
    public LoginUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Long userId = Long.valueOf(claims.getSubject());
        String phone = String.valueOf(claims.get("phone"));
        return new LoginUser(userId, phone);
    }

    /**
     * 校验 token 是否有效
     *
     * @param token token 字符串
     * @return 是否有效
     */
    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.debug("JWT 校验失败: {}", e.getMessage());
            return false;
        }
    }
}
