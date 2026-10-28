/**
 * 全局登录用户状态
 *
 * 说明：此前用户信息在组件 setup 时一次性读取 localStorage，
 * 登录成功后（App 早已挂载）不会重新读取，导致「第一次登录进入后不显示用户信息」。
 * 这里把用户态提升为模块级响应式对象，登录 / 退出 / 401 失效时统一同步。
 */
import { ref } from 'vue';
import { getStoredUser, type StoredUser } from '../utils/auth';

/** 当前登录用户（未登录为 null） */
const user = ref<StoredUser | null>(getStoredUser());

/** 从本地存储重新加载用户信息（登录成功后调用） */
export function refreshUser(): void {
  user.value = getStoredUser();
}

/** 设置用户信息（登录后调用，避免再次解析 localStorage） */
export function setUser(next: StoredUser | null): void {
  user.value = next;
}

/** 清空用户信息（退出登录 / token 失效时调用） */
export function clearUser(): void {
  user.value = null;
}

/** 在组件中使用当前用户 */
export function useUser() {
  return { user, refreshUser, setUser, clearUser };
}

/** 头像加载失败时的兜底文字（取昵称首字，否则手机号后两位） */
export function avatarFallbackText(stored: StoredUser | null): string {
  if (!stored) return '?';
  const nickname = (stored.nickname || '').trim();
  if (nickname) return nickname.charAt(0).toUpperCase();
  const phone = (stored.phone || '').trim();
  return phone ? phone.slice(-2) : '?';
}
