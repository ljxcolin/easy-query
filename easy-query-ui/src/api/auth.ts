import request from './request';
import type { Response } from '../types';

// 登录响应（登录注册合一）
export interface LoginResult {
  token: string;
  userId: number;
  phone: string;
  nickname: string;
  newUser: boolean;
}

export default {
  // 发送短信验证码（当前为模拟实现，响应中直接返回验证码）
  sendCode(phone: string): Promise<Response<{ code: string }>> {
    return request.post('/auth/send-code', { phone });
  },
  // 登录/注册（手机号 + 验证码，用户不存在时自动注册）
  login(phone: string, code: string): Promise<Response<LoginResult>> {
    return request.post('/auth/login', { phone, code });
  },
  // 获取当前登录用户信息
  me(): Promise<Response<{ userId: number; phone: string }>> {
    return request.get('/auth/me');
  }
};
