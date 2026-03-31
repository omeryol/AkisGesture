# <img src="docs/icon.svg" width="48" align="center"/> OpenSwipe

**开源安卓手势导航** — 通过无障碍权限（AccessibilityService）实现自定义手势导航，无需 Root。

无广告 · 无追踪 · 完全开源 · 永久免费

[English](README-en.md) | **中文**

---

### 功能

| 分类 | 功能 | 状态 |
|------|------|------|
| **规则引擎** | 二部图手势规则引擎（触发→动作的可配置映射） | ✅ |
| **规则控制** | 每条规则独立启用/禁用 | ✅ |
| **触发模式** | 每条规则独立配置触发模式（轻触/滑动，点击穿透） | ✅ |
| **持久化** | 规则持久化（DataStore） | ✅ |
| **预设方案** | 内置预设（iOS 风格 / Android 经典 / 媒体控制） | ✅ |
| **底部分段** | 底部边缘三段区域（左1/3、中1/3、右1/3）各配不同动作 | ✅ |
| **系统动作** | 18 种系统动作可选 | ✅ |
| **边缘配置** | 边缘触发宽度/高度可调 | ✅ |
| **规则编辑** | 规则详情编辑页（保存+删除） | ✅ |
| **现代 UI** | Jetpack Compose Material3 | ✅ |
| **品牌标识** | 品牌矢量图标 | ✅ |

### 🛡️ 隐私承诺

- ❌ **零广告** — 没有 AdMob、AppLovin、Facebook Ads
- ❌ **零追踪** — 没有 Firebase Analytics、Crashlytics
- ❌ **零网络** — App 不联网，不发送任何数据
- ✅ **完全开源** — 每一行代码都可审计

### 📱 安装

1. 从 [Releases](https://github.com/ARCJ137442/OpenSwipe/releases) 下载 APK
2. 安装到手机
3. 打开 App → 跟随权限引导开启无障碍服务
4. 从屏幕边缘滑动，开始使用！

### 🔧 技术架构

```
com.openswipe/
├── service/     → AccessibilityService + 保活 + 开机自启
├── overlay/     → TYPE_ACCESSIBILITY_OVERLAY 窗口管理
├── gesture/     → 状态机手势检测引擎
├── rule/        → 二部图规则引擎（触发 ↔ 动作映射）
├── action/      → sealed class 动作分发（18 种系统动作）
├── feedback/    → 贝塞尔曲线拉伸 + 振动反馈
└── ui/          → Jetpack Compose Material3
```

**技术栈**: Kotlin 2.1 · Jetpack Compose · Material3 · DataStore · Coroutines

**核心原理**:
```kotlin
// 通过无障碍服务执行系统级操作，无需 Root
performGlobalAction(GLOBAL_ACTION_BACK)     // 返回
performGlobalAction(GLOBAL_ACTION_HOME)     // 主页
performGlobalAction(GLOBAL_ACTION_RECENTS)  // 最近任务
```

### 🏗️ 构建

```bash
git clone https://github.com/ARCJ137442/OpenSwipe.git
cd OpenSwipe
./gradlew assembleDebug
# APK 输出: app/build/outputs/apk/debug/app-debug.apk
```

要求: Android Studio 2024.2+ · JDK 21 · Android SDK 35

### 📋 路线图

- [x] MVP: 左/右/底手势导航
- [x] 独立开关 + 实时生效
- [x] 底部高度可调 + 轻触/滑动模式
- [x] 二部图手势规则引擎 + 可配置映射
- [x] 规则持久化 + 每条规则独立启用/禁用
- [x] 预设方案（iOS 风格 / Android 经典 / 媒体控制）
- [x] 底部三段区域各配不同动作
- [x] 18 种系统动作
- [x] 规则详情编辑页（保存+删除）
- [x] 品牌矢量图标
- [ ] 贝塞尔曲线视觉反馈 ([#3](https://github.com/ARCJ137442/OpenSwipe/issues/3))
- [ ] 按应用配置
- [ ] F-Droid 上架

### 📄 许可证

[MIT](LICENSE)

---

> 最后更新: 2026-03-31

> ✨ *人人可DIY的手势导航 — 开源拯救世界* 🚀
