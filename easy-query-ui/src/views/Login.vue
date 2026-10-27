<template>
  <div class="flex min-h-screen items-center justify-center bg-slate-950 px-6 text-slate-200 antialiased">
    <div class="pointer-events-none fixed -top-32 left-1/2 h-[400px] w-[700px] -translate-x-1/2 rounded-full bg-sky-500/10 blur-3xl"></div>

    <div class="relative w-full max-w-md">
      <div class="mb-8 text-center">
        <div class="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-sky-500 to-indigo-600 text-lg font-bold text-white">EQ</div>
        <h1 class="text-2xl font-bold text-white">登录 EasyQuery</h1>
        <p class="mt-2 text-sm text-slate-400">未注册的手机号验证通过后将自动创建账号</p>
      </div>

      <div class="rounded-2xl border border-white/10 bg-white/[0.04] p-8 backdrop-blur">
        <!-- 手机号 -->
        <div>
          <label class="mb-2 block text-sm font-medium text-slate-300">手机号</label>
          <input
            v-model.trim="phone"
            type="tel"
            maxlength="11"
            placeholder="请输入 11 位手机号"
            class="w-full rounded-xl border border-white/10 bg-slate-900/60 px-4 py-3 text-white placeholder-slate-500 outline-none transition focus:border-sky-500/60 focus:ring-2 focus:ring-sky-500/20"
            :disabled="loading"
          />
        </div>

        <!-- 验证码 -->
        <div class="mt-5">
          <label class="mb-2 block text-sm font-medium text-slate-300">短信验证码</label>
          <div class="flex gap-3">
            <input
              v-model.trim="code"
              type="text"
              maxlength="6"
              placeholder="6 位验证码"
              class="min-w-0 flex-1 rounded-xl border border-white/10 bg-slate-900/60 px-4 py-3 text-white placeholder-slate-500 outline-none transition focus:border-sky-500/60 focus:ring-2 focus:ring-sky-500/20"
              :disabled="loading"
              @keyup.enter="handleLogin"
            />
            <button
              class="shrink-0 rounded-xl border border-sky-500/40 bg-sky-500/10 px-4 text-sm font-medium text-sky-400 transition hover:bg-sky-500/20 disabled:cursor-not-allowed disabled:opacity-50"
              :disabled="countdown > 0 || sending"
              @click="handleSendCode"
            >
              {{ sendButtonText }}
            </button>
          </div>
        </div>

        <!-- 错误提示 -->
        <p v-if="errorMsg" class="mt-4 rounded-lg border border-red-500/30 bg-red-500/10 px-3 py-2 text-sm text-red-400">
          {{ errorMsg }}
        </p>

        <!-- 登录按钮 -->
        <button
          class="mt-6 w-full rounded-xl bg-gradient-to-r from-sky-500 to-indigo-500 py-3 font-semibold text-white shadow-lg shadow-sky-500/25 transition hover:brightness-110 disabled:cursor-not-allowed disabled:opacity-60"
          :disabled="loading"
          @click="handleLogin"
        >
          {{ loading ? '登录中...' : '登录 / 注册' }}
        </button>

        <p class="mt-5 text-center text-xs leading-relaxed text-slate-500">
          登录即代表同意
          <a href="javascript:void(0)" class="text-sky-400 hover:underline">服务协议</a>
          与
          <a href="javascript:void(0)" class="text-sky-400 hover:underline">隐私政策</a>
        </p>
      </div>

      <div class="mt-6 text-center">
        <RouterLink to="/" class="text-sm text-slate-500 transition hover:text-slate-300">← 返回首页</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import '../styles/tailwind.css';
import { computed, onBeforeUnmount, ref } from 'vue';
import { useRoute, useRouter, RouterLink } from 'vue-router';
import { ElMessage } from 'element-plus';
import authApi from '../api/auth';
import { setLogin } from '../utils/auth';

const route = useRoute();
const router = useRouter();

const phone = ref('');
const code = ref('');
const loading = ref(false);
const sending = ref(false);
const errorMsg = ref('');
const countdown = ref(0);

let timer: ReturnType<typeof setInterval> | null = null;

const sendButtonText = computed(() => {
  if (sending.value) return '发送中...';
  if (countdown.value > 0) return `${countdown.value}s 后重发`;
  return '获取验证码';
});

/** 发送验证码（模拟实现：响应中返回验证码并自动填充） */
async function handleSendCode() {
  errorMsg.value = '';
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    errorMsg.value = '请输入正确的手机号';
    return;
  }
  sending.value = true;
  try {
    const res = await authApi.sendCode(phone.value);
    if (res.code === 200 && res.data) {
      // 模拟通道：自动填充验证码
      code.value = res.data.code;
      ElMessage.success('验证码已发送（演示模式已自动填充）');
      startCountdown();
    } else {
      errorMsg.value = res.message || '验证码发送失败';
    }
  } catch (e: any) {
    errorMsg.value = e?.response?.data?.message || '验证码发送失败，请稍后重试';
  } finally {
    sending.value = false;
  }
}

/** 登录/注册（合一） */
async function handleLogin() {
  errorMsg.value = '';
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    errorMsg.value = '请输入正确的手机号';
    return;
  }
  if (!code.value) {
    errorMsg.value = '请输入验证码';
    return;
  }
  loading.value = true;
  try {
    const res = await authApi.login(phone.value, code.value);
    if (res.code === 200 && res.data) {
      setLogin(res.data.token, {
        userId: res.data.userId,
        phone: res.data.phone,
        nickname: res.data.nickname
      });
      ElMessage.success(res.data.newUser ? '注册成功，欢迎加入！' : '登录成功');
      const redirect = (route.query.redirect as string) || '/tool';
      router.push(redirect);
    } else {
      errorMsg.value = res.message || '登录失败';
    }
  } catch (e: any) {
    errorMsg.value = e?.response?.data?.message || '登录失败，请稍后重试';
  } finally {
    loading.value = false;
  }
}

/** 启动 60 秒倒计时 */
function startCountdown() {
  countdown.value = 60;
  timer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0 && timer) {
      clearInterval(timer);
      timer = null;
    }
  }, 1000);
}

onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
});
</script>
