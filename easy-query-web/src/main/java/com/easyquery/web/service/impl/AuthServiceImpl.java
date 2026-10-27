package com.easyquery.web.service.impl;

import com.easyquery.web.entity.UserEntity;
import com.easyquery.web.model.LoginRequest;
import com.easyquery.web.model.LoginResponse;
import com.easyquery.web.repository.UserRepository;
import com.easyquery.web.service.AuthService;
import com.easyquery.web.service.SmsCodeService;
import com.easyquery.web.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

/**
 * 认证服务实现
 *
 * @author easy-query
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    /** 中国大陆手机号格式 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private final UserRepository userRepository;

    private final SmsCodeService smsCodeService;

    private final JwtUtil jwtUtil;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository, SmsCodeService smsCodeService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.smsCodeService = smsCodeService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public String sendCode(String phone) {
        checkPhone(phone);
        return smsCodeService.sendCode(phone);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String phone = request.getPhone();
        String code = request.getCode();
        checkPhone(phone);
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("验证码不能为空");
        }

        // 校验验证码（一次性）
        smsCodeService.verify(phone, code);

        // 登录注册合一：不存在则自动注册
        boolean isNewUser = false;
        UserEntity user = userRepository.findByPhone(phone).orElse(null);
        if (user == null) {
            user = new UserEntity();
            user.setPhone(phone);
            user.setNickname("用户" + phone.substring(phone.length() - 4));
            user.setStatus(1);
            user = userRepository.save(user);
            isNewUser = true;
            logger.info("新用户注册, phone={}", phone);
        } else if (user.getStatus() != null && user.getStatus() != 1) {
            throw new IllegalStateException("账号已被禁用，请联系管理员");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getPhone());
        return new LoginResponse(token, user.getId(), user.getPhone(), user.getNickname(), isNewUser);
    }

    /**
     * 校验手机号格式
     *
     * @param phone 手机号
     */
    private void checkPhone(String phone) {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
    }
}
