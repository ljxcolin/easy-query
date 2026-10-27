/**
 * 主题管理：亮色 / 暗色切换，基于 Element Plus 的 dark css-vars 体系
 */

const THEME_KEY = 'easy_query_theme';

export type ThemeMode = 'light' | 'dark';

/** 读取已保存的主题偏好 */
export function getSavedTheme(): ThemeMode {
  const saved = localStorage.getItem(THEME_KEY);
  return saved === 'dark' ? 'dark' : 'light';
}

/** 应用主题到 document 根元素 */
export function applyTheme(mode: ThemeMode): void {
  document.documentElement.classList.toggle('dark', mode === 'dark');
}

/** 持久化并应用主题 */
export function setTheme(mode: ThemeMode): void {
  localStorage.setItem(THEME_KEY, mode);
  applyTheme(mode);
}

/** 切换主题并返回新模式 */
export function toggleTheme(): ThemeMode {
  const next: ThemeMode = document.documentElement.classList.contains('dark') ? 'light' : 'dark';
  setTheme(next);
  return next;
}

/** 应用启动时调用：恢复上次主题偏好 */
export function initTheme(): void {
  applyTheme(getSavedTheme());
}
