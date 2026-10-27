package com.easyquery.web.auth;

/**
 * 当前登录用户上下文（基于 ThreadLocal）
 * 请求结束时必须调用 {@link #clear()} 防止内存泄漏（拦截器 afterCompletion 中处理）
 *
 * @author easy-query
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> CONTEXT = new ThreadLocal<>();

    private UserContext() {
    }

    /**
     * 设置当前登录用户
     *
     * @param loginUser 登录用户
     */
    public static void set(LoginUser loginUser) {
        CONTEXT.set(loginUser);
    }

    /**
     * 获取当前登录用户
     *
     * @return 登录用户，未登录返回 null
     */
    public static LoginUser get() {
        return CONTEXT.get();
    }

    /**
     * 清理上下文
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
