import axios, { AxiosInstance } from 'axios';
import type { Response } from '../types';
import { getToken, clearLogin } from '../utils/auth';
import { clearUser } from '../stores/user';

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器：统一注入 JWT token
request.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const body = response.data as Response;
    // 业务码 401：未登录或登录已过期，清除本地登录态并跳转登录页
    if (body && body.code === 401) {
      clearLogin();
      clearUser();
      const current = window.location.hash.slice(1) || '/';
      if (!current.startsWith('/login')) {
        window.location.hash = `#/login?redirect=${encodeURIComponent(current)}`;
      }
    }
    return response.data;
  },
  (error) => {
    console.error('Request error:', error);
    return Promise.reject(error);
  }
);

export default request;
