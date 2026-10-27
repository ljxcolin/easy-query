/**
 * 登录态管理：token 与用户信息的本地存取
 */

const TOKEN_KEY = 'easy_query_token';
const USER_KEY = 'easy_query_user';

export interface StoredUser {
  userId: number;
  phone: string;
  nickname: string;
}

/** 获取 token */
export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

/** 保存 token 与用户信息 */
export function setLogin(token: string, user: StoredUser): void {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

/** 清除登录态 */
export function clearLogin(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

/** 获取当前用户信息 */
export function getStoredUser(): StoredUser | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as StoredUser;
  } catch {
    return null;
  }
}

/** 是否已登录（本地判断，有效性以后端校验为准） */
export function isLoggedIn(): boolean {
  return !!getToken();
}
