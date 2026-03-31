# <img src="docs/icon.svg" width="48" align="center"/> OpenSwipe

**开源安卓手势导航** — 通过无障碍权限（AccessibilityService）实现自定义手势导航。

无广告 · 无追踪 · 完全开源 · 永久免费

---

## ✨ 功能

| 手势 | 动作 | 状态 |
|------|------|------|
| ← 左边缘向右滑 | 返回 | ✅ |
| → 右边缘向左滑 | 返回 | ✅ |
| ↑ 底部上滑 | 主页 / 最近任务 | ✅ |
| ⚙️ 独立开关 | 左/右/底部分别启停 | ✅ |
| 📏 底部高度可调 | 20~80dp 滑块调节 | ✅ |
| 👆 触发模式 | 轻触 / 滑动（点击穿透） | ✅ |

## 🛡️ 隐私承诺

- ❌ **零广告** — 没有 AdMob、AppLovin、Facebook Ads
- ❌ **零追踪** — 没有 Firebase Analytics、Crashlytics
- ❌ **零网络** — App 不联网，不发送任何数据
- ✅ **完全开源** — 每一行代码都可审计

## 📱 安装

1. 从 [Releases](https://github.com/ARCJ137442/OpenSwipe/releases) 下载 APK
2. 安装到手机
3. 打开 App → 跟随权限引导开启无障碍服务
4. 从屏幕边缘滑动，开始使用！

## 🔧 技术架构

```
com.openswipe/
├── service/     → AccessibilityService + 保活 + 开机自启
├── overlay/     → TYPE_ACCESSIBILITY_OVERLAY 窗口管理
├── gesture/     → 状态机手势检测引擎
├── action/      → sealed class 动作分发
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

## 🏗️ 构建

```bash
git clone https://github.com/ARCJ137442/OpenSwipe.git
cd OpenSwipe
./gradlew assembleDebug
# APK 输出: app/build/outputs/apk/debug/app-debug.apk
```

要求: Android Studio 2024.2+ · JDK 21 · Android SDK 35

## 📋 路线图

- [x] MVP: 左/右/底手势导航
- [x] 独立开关 + 实时生效
- [x] 底部高度可调 + 轻触/滑动模式
- [ ] 贝塞尔曲线视觉反馈 ([#3](https://github.com/ARCJ137442/OpenSwipe/issues/3))
- [ ] 自定义动作映射
- [ ] 按应用配置
- [ ] F-Droid 上架

## 📄 License

[MIT](LICENSE)

---

> ✨ *人人可DIY的手势导航 — 开源拯救世界* 🚀
