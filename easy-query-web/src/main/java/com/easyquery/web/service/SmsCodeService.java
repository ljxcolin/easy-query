package com.easyquery.web.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 短信验证码服务（当前为模拟实现）
 * <p>
 * 验证码保存在内存中，5 分钟过期，同一手机号 60 秒内只能发送一次。
 * 生产环境需替换为真实短信通道 + Redis 存储。
 * </p>
 *
 * @author easy-query
 */
@Service
public class SmsCodeService {

    /** 验证码有效期（分钟） */
    private static final int CODE_EXPIRE_MINUTES = 5;

    /** 同一手机号再次发送的最小间隔（秒） */
    private static final int RESEND_INTERVAL_SECONDS = 60;

    /**
     * 验证码记录
     *
     * @param code       验证码
     * @param expireAt   过期时间
     * @param nextSendAt 下次允许发送时间
     */
    private record CodeEntry(String code, LocalDateTime expireAt, LocalDateTime nextSendAt) {
    }

    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();

    /**
     * 发送验证码（模拟实现：仅生成并存储，由调用方决定是否回显）
     *
     * @param phone 手机号
     * @return 验证码（模拟通道直接返回，供前端自动填充；接入真实短信后必须移除）
     */
    public String sendCode(String phone) {
        CodeEntry old = codeStore.get(phone);
        if (old != null && LocalDateTime.now().isBefore(old.nextSendAt())) {
            throw new IllegalStateException("发送过于频繁，请 " + RESEND_INTERVAL_SECONDS + " 秒后再试");
        }
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        LocalDateTime now = LocalDateTime.now();
        codeStore.put(phone, new CodeEntry(
                code,
                now.plusMinutes(CODE_EXPIRE_MINUTES),
                now.plusSeconds(RESEND_INTERVAL_SECONDS)
        ));
        return code;
    }

    /**
     * 校验验证码，校验通过后立即删除（一次性使用）
     *
     * @param phone 手机号
     * @param code  用户输入的验证码
     */
    public void verify(String phone, String code) {
        CodeEntry entry = codeStore.get(phone);
        if (entry == null || LocalDateTime.now().isAfter(entry.expireAt())) {
            codeStore.remove(phone);
            throw new IllegalStateException("验证码已过期，请重新获取");
        }
        if (!entry.code().equals(code)) {
            throw new IllegalStateException("验证码错误");
        }
        codeStore.remove(phone);
    }
}
