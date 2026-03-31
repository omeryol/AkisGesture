# OpenSwipe Bug 修复方案

## Bug #1: 左右滑动返回异常

### 根因分析

经过完整代码审查，发现 **三个问题** 共同导致左右滑动异常：

#### 问题 1: dampingFactor 导致位移被过度衰减（核心问题）

**文件**: `EdgeGestureDetector.kt` 第 75 行
```kotlin
val dampedDisplacement = rawDisplacement / config.dampingFactor
```

`GestureConfig.kt` 中 `dampingFactor = 5.0f`，`minSwipeThresholdPx = 15f`。

用户从左边缘向右滑动 60px（正常手指滑动距离），rawDisplacement = 60，damped = 60/5 = 12，小于 minSwipeThresholdPx(15)，**永远不会触发 EdgeSwipe**，而是落入 VerticalSwipe 或 Tap 分支（第 101-110 行），最终映射为 `ActionType.None`（GestureEngine.kt 第 174-175 行）。

要触发 EdgeSwipe 的最短滑动：`15 * 5 = 75px`（约 25dp），这对于 20dp 宽的触发区域来说几乎不可能——手指在 20dp 窄条内要滑 75px 才会触发。

#### 问题 2: scaledTouchSlop 硬编码

**文件**: `EdgeGestureDetector.kt` 第 18 行
```kotlin
private val scaledTouchSlop = 24  // reasonable default, ~8dp at mdpi
```

硬编码 24px，在高密度屏幕（如 xxhdpi，density=3）上实际仅约 8dp，但在 mdpi 上确实是 24dp——过高。应使用 `ViewConfiguration.get(context).scaledTouchSlop`（通常 ~8dp = 24px@xxxhdpi, 16px@xhdpi）。当前值在低密度设备上会使手势过于迟钝。

#### 问题 3: 底部手势参数传反

**文件**: `GestureEngine.kt` 第 127 行
```kotlin
val window = OverlayWindowFactory.createEdgeSensor(
    overlayManager.context, Edge.BOTTOM, screenWidth, bottomHeightPx, ...
)
```

`createEdgeSensor` 签名是 `(context, edge, widthPx, heightPx, ...)`。底部传入 `screenWidth` 作为 width、`bottomHeightPx` 作为 height——这是正确的（底部条宽=屏宽，高=48dp）。此处无误。

### 修复方案

#### 修改 1: 调整阈值参数（`GestureConfig.kt`）

```kotlin
// 原
val dampingFactor: Float = 5.0f,
val minSwipeThresholdPx: Float = 15f,

// 改为
val dampingFactor: Float = 2.0f,
val minSwipeThresholdPx: Float = 30f,
val peakThreshold: Float = 200f,  // 原 100f
```

**理由**: dampingFactor=2 时，用户滑动 60px → damped=30 → 刚好达到 minSwipeThresholdPx(30)，触发 EdgeSwipe。peakThreshold 对应提升，使短滑/长滑的区分仍然合理。

#### 修改 2: 使用系统 touchSlop（`EdgeGestureDetector.kt`）

构造函数增加 `scaledTouchSlop` 参数，由 `GestureEngine` 从 `ViewConfiguration` 获取后传入：

```kotlin
class EdgeGestureDetector(
    private val edge: Edge,
    private val config: GestureConfig,
    private val scaledTouchSlop: Int,  // 新增
    private val onGestureResult: (GestureResult) -> Unit,
)
```

`GestureEngine.createDetector()` 中：
```kotlin
val touchSlop = ViewConfiguration.get(overlayManager.context).scaledTouchSlop
return EdgeGestureDetector(edge, configCopy, touchSlop, ::handleGestureResult)
```

---

## Bug #2: 设置页面无法点开

### 根因分析

**文件**: `MainActivity.kt` 第 122-124 行

```kotlin
NavigationBarItem(
    ...
    selected = currentRoute == "settings",
    onClick = {
        // Phase 2: 设置页面
    },
)
```

**onClick 回调为空**——点击"设置"底部导航项没有任何导航操作。此外，NavHost 中没有注册 `"settings"` 路由（只有 `"home"` 和 `"permissions"`）。

这是一个 **功能未实现** 的问题，不是路由配置错误。底部导航栏展示了"设置"入口但没有对应的页面和导航逻辑。

### 修复方案

#### 方案 A: 快速修复（创建最小设置页面）

1. **新建** `app/src/main/java/com/openswipe/ui/screen/SettingsScreen.kt`

   创建包含基本手势配置的 Composable（可复用 HomeScreen 中的 SettingSwitch 组件）。

2. **修改** `MainActivity.kt`：

   NavHost 中添加路由：
   ```kotlin
   composable("settings") {
       SettingsScreen()
   }
   ```

   底部导航栏 onClick 添加导航：
   ```kotlin
   onClick = {
       if (currentRoute != "settings") {
           navController.navigate("settings") {
               popUpTo("home") { saveState = true }
               launchSingleTop = true
               restoreState = true
           }
       }
   },
   ```

#### 方案 B: 最小修复（重定向到已有页面）

如果设置页暂不需要，将"设置"导航项改为跳转到权限页面：

```kotlin
onClick = {
    navController.navigate("permissions")
},
```

**推荐方案 A**，因为底部导航栏的"设置"应有独立页面，权限引导是一次性流程不适合放在常驻导航中。

---

## 修改顺序

| 顺序 | 文件 | 改动 | 关联 Bug |
|------|------|------|----------|
| 1 | `GestureConfig.kt` | dampingFactor=2.0, minSwipeThresholdPx=30, peakThreshold=200 | #1 |
| 2 | `EdgeGestureDetector.kt` | 构造函数新增 scaledTouchSlop 参数，删除硬编码 | #1 |
| 3 | `GestureEngine.kt` | createDetector() 传入 ViewConfiguration.scaledTouchSlop | #1 |
| 4 | 新建 `SettingsScreen.kt` | 创建设置页面 Composable | #2 |
| 5 | `MainActivity.kt` | NavHost 添加 "settings" 路由 + 底部栏 onClick 导航 | #2 |

步骤 1-3 修复 Bug #1（相互依赖，需一起改）；步骤 4-5 修复 Bug #2（相互依赖，需一起改）。两组之间无依赖，可并行开发。
