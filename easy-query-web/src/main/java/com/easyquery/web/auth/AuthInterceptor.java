package com.easyquery.web.auth;

import com.easyquery.web.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * JWT 鉴权拦截器
 * <p>
 * 校验 Authorization: Bearer &lt;token&gt;，通过后把登录用户放入 {@link UserContext}。
 * 鉴权失败按项目约定返回 HTTP 200 + 业务码 401，由前端统一拦截处理。
 * </p>
 *
 * @author easy-query
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    private final ObjectMapper objectMapper;

    @Autowired
    public AuthInterceptor(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 CORS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return reject(response);
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        try {
            LoginUser loginUser = jwtUtil.parseToken(token);
            UserContext.set(loginUser);
            return true;
        } catch (Exception e) {
            return reject(response);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理 ThreadLocal，防止线程复用导致内存泄漏
        UserContext.clear();
    }

    /**
     * 鉴权失败响应（保持项目约定：HTTP 200 + 业务码 401）
     */
    private boolean reject(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(
                Map.of("code", 401, "message", "未登录或登录已过期", "data", "")
        ));
        return false;
    }
}
