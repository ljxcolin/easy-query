package com.easyquery.web.controller;

import com.easyquery.web.auth.LoginUser;
import com.easyquery.web.auth.UserContext;
import com.easyquery.web.model.LoginRequest;
import com.easyquery.web.model.LoginResponse;
import com.easyquery.web.model.Response;
import com.easyquery.web.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 登录注册合一（手机号 + 短信验证码），当前验证码为模拟发送，接口直接返回验证码供前端自动填充
 */
@RestController
@RequestMapping("/auth")
public class AuthController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    /**
     * 发送短信验证码
     * ⚠️ 模拟实现：响应中直接返回验证码，接入真实短信后必须移除 code 字段
     *
     * @param phone 手机号
     * @return 验证码
     */
    @PostMapping("/send-code")
    public Response<Map<String, String>> sendCode(@RequestBody Map<String, String> body) {
        try {
            String phone = body.get("phone");
            String code = authService.sendCode(phone);
            logger.info("模拟发送短信验证码, phone={}", phone);
            Map<String, String> data = new HashMap<>();
            data.put("code", code);
            return Response.success(data);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Response.error(400, e.getMessage());
        } catch (Exception e) {
            logger.error("发送验证码失败", e);
            return Response.error(500, "发送验证码失败：" + e.getMessage());
        }
    }

    /**
     * 登录/注册（合一）：手机号 + 验证码，用户不存在时自动注册
     *
     * @param request 登录请求
     * @return token 与用户信息
     */
    @PostMapping("/login")
    public Response<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            return Response.success(authService.login(request));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Response.error(400, e.getMessage());
        } catch (Exception e) {
            logger.error("登录失败", e);
            return Response.error(500, "登录失败：" + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户信息（需携带 token）
     *
     * @return 当前用户
     */
    @GetMapping("/me")
    public Response<Map<String, Object>> me() {
        LoginUser loginUser = UserContext.get();
        Map<String, Object> data = new HashMap<>();
        data.put("userId", loginUser.userId());
        data.put("phone", loginUser.phone());
        return Response.success(data);
    }
}
