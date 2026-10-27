<template>
  <div class="min-h-screen bg-slate-950 text-slate-200 antialiased">
    <!-- 顶部导航 -->
    <header class="fixed top-0 left-0 right-0 z-50 border-b border-white/5 bg-slate-950/80 backdrop-blur">
      <div class="mx-auto flex h-16 max-w-6xl items-center justify-between px-6">
        <div class="flex items-center gap-2.5">
          <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-sky-500 to-indigo-600 font-bold text-white">EQ</div>
          <span class="text-lg font-semibold tracking-wide text-white">EasyQuery</span>
        </div>
        <nav class="hidden items-center gap-8 text-sm text-slate-400 md:flex">
          <a href="#features" class="transition hover:text-white">产品能力</a>
          <a href="#workflow" class="transition hover:text-white">工作流程</a>
          <a href="#start" class="transition hover:text-white">快速开始</a>
        </nav>
        <button
          class="rounded-lg border border-sky-500/40 bg-sky-500/10 px-4 py-1.5 text-sm font-medium text-sky-400 transition hover:bg-sky-500/20"
          @click="handleStart"
        >
          {{ isLoggedIn() ? '进入工作台' : '登录 / 注册' }}
        </button>
      </div>
    </header>

    <!-- Hero -->
    <section class="relative overflow-hidden pt-36 pb-24">
      <div class="pointer-events-none absolute -top-40 left-1/2 h-[480px] w-[820px] -translate-x-1/2 rounded-full bg-sky-500/15 blur-3xl"></div>
      <div class="relative mx-auto max-w-6xl px-6 text-center">
        <div class="mx-auto mb-6 inline-flex items-center gap-2 rounded-full border border-sky-500/30 bg-sky-500/10 px-4 py-1.5 text-xs text-sky-300">
          <span class="h-1.5 w-1.5 animate-pulse rounded-full bg-sky-400"></span>
          数据库分库分表查询平台
        </div>
        <h1 class="mx-auto max-w-3xl text-5xl font-bold leading-tight text-white md:text-6xl">
          让分库分表后的
          <span class="bg-gradient-to-r from-sky-400 to-indigo-400 bg-clip-text text-transparent">SQL 查询</span>
          像单表一样简单
        </h1>
        <p class="mx-auto mt-6 max-w-2xl text-lg leading-relaxed text-slate-400">
          EasyQuery 是一个带透明分片能力的 Web SQL 客户端。数据源与分片规则配置即时生效，
          无需改造成分片中间件，写一条普通 SQL，平台自动完成路由改写与结果归并。
        </p>
        <div id="start" class="mt-10 flex items-center justify-center gap-4">
          <button
            class="rounded-xl bg-gradient-to-r from-sky-500 to-indigo-500 px-8 py-3.5 text-base font-semibold text-white shadow-lg shadow-sky-500/25 transition hover:shadow-sky-500/40 hover:brightness-110"
            @click="handleStart"
          >
            立即使用
          </button>
          <a
            href="#features"
            class="rounded-xl border border-white/10 px-8 py-3.5 text-base font-medium text-slate-300 transition hover:border-white/25 hover:text-white"
          >
            了解更多
          </a>
        </div>
      </div>
    </section>

    <!-- 产品亮点 -->
    <section id="features" class="mx-auto max-w-6xl px-6 py-20">
      <h2 class="text-center text-3xl font-bold text-white">核心能力</h2>
      <p class="mt-3 text-center text-slate-400">从数据源接入到分片查询，开箱即用</p>
      <div class="mt-14 grid gap-6 md:grid-cols-3">
        <div
          v-for="f in features"
          :key="f.title"
          class="group rounded-2xl border border-white/10 bg-white/[0.03] p-7 transition hover:border-sky-500/40 hover:bg-sky-500/[0.06]"
        >
          <div class="flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br from-sky-500/20 to-indigo-500/20 text-xl">
            {{ f.icon }}
          </div>
          <h3 class="mt-5 text-lg font-semibold text-white">{{ f.title }}</h3>
          <p class="mt-2.5 text-sm leading-relaxed text-slate-400">{{ f.desc }}</p>
        </div>
      </div>
    </section>

    <!-- 工作流程 -->
    <section id="workflow" class="mx-auto max-w-6xl px-6 py-20">
      <h2 class="text-center text-3xl font-bold text-white">一次查询的工作流程</h2>
      <div class="mt-14 grid gap-4 md:grid-cols-5">
        <div
          v-for="(step, i) in steps"
          :key="step.title"
          class="relative rounded-2xl border border-white/10 bg-white/[0.03] p-6"
        >
          <span class="text-xs font-semibold text-sky-400">STEP {{ i + 1 }}</span>
          <h3 class="mt-2 font-semibold text-white">{{ step.title }}</h3>
          <p class="mt-1.5 text-xs leading-relaxed text-slate-400">{{ step.desc }}</p>
        </div>
      </div>
    </section>

    <!-- 底部 CTA -->
    <section class="mx-auto max-w-6xl px-6 pb-24">
      <div class="relative overflow-hidden rounded-3xl border border-sky-500/20 bg-gradient-to-br from-sky-500/10 via-slate-900 to-indigo-500/10 px-8 py-16 text-center">
        <div class="pointer-events-none absolute -bottom-24 left-1/2 h-64 w-[600px] -translate-x-1/2 rounded-full bg-sky-500/10 blur-3xl"></div>
        <h2 class="relative text-3xl font-bold text-white">准备好开始了吗？</h2>
        <p class="relative mt-3 text-slate-400">手机号验证码登录，一分钟接入你的第一个分片数据源</p>
        <button
          class="relative mt-8 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-500 px-10 py-3.5 text-base font-semibold text-white shadow-lg shadow-sky-500/25 transition hover:brightness-110"
          @click="handleStart"
        >
          立即使用
        </button>
      </div>
    </section>

    <footer class="border-t border-white/5 py-8 text-center text-sm text-slate-500">
      EasyQuery — 开源分库分表查询平台
    </footer>
  </div>
</template>

<script setup lang="ts">
import '../styles/tailwind.css';
import { isLoggedIn } from '../utils/auth';
import { useRouter } from 'vue-router';

const router = useRouter();

const features = [
  { icon: '⚡', title: 'SQL 透明改写', desc: '基于 JSqlParser 的 AST 改写，保留占位符语义。多分片查询自动拼接 UNION ALL，绑定表 JOIN 自动对齐分片后缀。' },
  { icon: '🧩', title: '多策略分片', desc: '内置 HASH / RANGE / YEAR 等分片策略，规则存于数据库、支持运行时增删改，即时生效无需重启应用。' },
  { icon: '🖥️', title: '可视化工作台', desc: '数据源管理、分片规则配置、SQL 查询一体化管理台，配合 Monaco 编辑器，改表跑 SQL 一个页面搞定。' },
  { icon: '🔌', title: '多数据库适配', desc: '适配器模式接入不同数据库方言，内置 MySQL / PostgreSQL 适配实现，连接池参数按数据源独立配置。' },
  { icon: '🚀', title: '轻量易集成', desc: '核心库框架无关、不依赖 Spring 容器，既可独立部署为查询平台，也可作为 jar 嵌入既有 Java 应用。' },
  { icon: '🛡️', title: '安全可控', desc: '账号手机号验证码登录，JWT 鉴权保护全部工具接口，敏感的数据源凭据与查询能力仅对登录用户开放。' }
];

const steps = [
  { title: '接入数据源', desc: '在管理台录入 JDBC 连接信息，连接池即刻建立' },
  { title: '配置分片规则', desc: '选择策略类型，绑定逻辑表与分片键' },
  { title: '编写 SQL', desc: '像操作单表一样书写普通 SQL 查询' },
  { title: '自动改写路由', desc: '解析 WHERE 条件定位目标物理分片' },
  { title: '结果归并返回', desc: '多分片结果自动合并、排序与分页' }
];

/** 立即使用：已登录进工作台，未登录去登录页 */
function handleStart() {
  if (isLoggedIn()) {
    router.push('/tool');
  } else {
    router.push('/login');
  }
}
</script>
