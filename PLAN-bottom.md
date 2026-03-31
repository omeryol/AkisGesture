# 底部手势增强：敏感区可调 + 触发模式切换

## 1. 功能设计

### 1.1 底部敏感区范围可调

用户可通过 Settings 页面的滑块调整底部触发区域高度，范围 **20dp ~ 80dp**，默认 48dp（当前值）。调整后实时生效，overlay 窗口重建。

### 1.2 触发模式切换：轻触 vs 滑动

| 模式 | 行为 | 适用场景 |
|------|------|----------|
| **轻触**（默认，当前行为） | 手指触碰底部区域即开始检测，所有触摸事件被 overlay 消费 | 追求灵敏度，不介意底部区域被占用 |
| **滑动** | 只有明确上滑才触发手势；点击/双击穿透到下层 App | 底部有按钮的 App（如浏览器导航栏） |

---

## 2. 技术方案

### 2.1 敏感区高度可调

直接方案：
- `GestureConfig.bottomTriggerHeightDp` 已存在，改为从 DataStore 读取
- `GestureEngine.applyConfigDiff()` 检测高度变化时，移除并重建底部 overlay 窗口

### 2.2 滑动模式的事件穿透方案

#### 方案选择：**方案 C — ACTION_DOWN 消费 + 点击重放**

**理由：**

- **方案 A（FLAG_NOT_TOUCHABLE 动态切换）**：不可行。WindowManager flag 切换有延迟，无法在单次触摸序列中生效。
- **方案 B（ACTION_DOWN return false）**：不可行。Android 触摸分发规则：如果 ACTION_DOWN 返回 false，后续 MOVE/UP 不会再派发给该 View，无法在后续判断是否为上滑。
- **方案 D（极窄触发条 5dp）**：体验差。5dp 约 2mm，手指很难精确从这么窄的区域起始上滑，误触率高、成功率低。
- **方案 C** 是唯一兼顾可靠检测与事件穿透的方案：
  1. ACTION_DOWN 时 return true（获得后续事件流）
  2. ACTION_MOVE 中判断：若 `dy > touchSlop && |dy| > |dx|`（明确上滑），进入手势检测
  3. ACTION_UP 时若判定为点击（总位移 < touchSlop），通过 `AccessibilityService.dispatchGesture()` 向原始坐标重放一次 tap
  4. 点击重放使用 `GestureDescription.Builder` 构建一个 duration=1ms 的 tap，目标坐标为 ACTION_DOWN 的 rawX/rawY

**关键实现细节：**
- 重放 tap 需要 AccessibilityService 实例引用，通过 `GestureAccessibilityService` 的静态引用获取
- 重放的坐标使用 `event.rawX/rawY`（屏幕绝对坐标），与 `GestureDescription.StrokeDescription` 的 `Path` 坐标系一致
- 滑动模式下，MOVE 阶段未超过 touchSlop 时不做任何视觉反馈，避免误触感

---

## 3. 需要修改的文件清单

### 3.1 GestureConfig.kt

```diff
+ import androidx.datastore.preferences.core.floatPreferencesKey
+ import androidx.datastore.preferences.core.stringPreferencesKey

  data class GestureConfig(
      ...
      val bottomTriggerHeightDp: Float = 48f,
+     val bottomTriggerMode: BottomTriggerMode = BottomTriggerMode.TOUCH,
      ...
  ) {
      companion object {
          ...
+         val KEY_BOTTOM_TRIGGER_HEIGHT = floatPreferencesKey("bottom_trigger_height_dp")
+         val KEY_BOTTOM_TRIGGER_MODE = stringPreferencesKey("bottom_trigger_mode")
      }
  }

+ enum class BottomTriggerMode {
+     TOUCH,  // 轻触模式（当前行为）
+     SWIPE,  // 滑动模式（点击穿透）
+ }
```

### 3.2 OpenSwipeApp.kt

- `gestureConfigFlow` 的 map 中读取新的 DataStore key：
  - `KEY_BOTTOM_TRIGGER_HEIGHT` -> `bottomTriggerHeightDp`（默认 48f）
  - `KEY_BOTTOM_TRIGGER_MODE` -> `bottomTriggerMode`（默认 TOUCH）
- 新增方法：
  - `suspend fun updateBottomTriggerHeight(dp: Float)`
  - `suspend fun updateBottomTriggerMode(mode: BottomTriggerMode)`

### 3.3 EdgeGestureDetector.kt

- 构造函数新增参数 `triggerMode: BottomTriggerMode`（仅底部 edge 使用）
- `onTouchEvent()` 修改：
  - **TOUCH 模式**：行为不变，始终 return true
  - **SWIPE 模式**：
    - ACTION_DOWN: return true，记录 downX/downY，状态设为 AWAITING_DIRECTION
    - ACTION_MOVE: 若总位移 > touchSlop，判断方向：
      - `|dy| > |dx| && dy < 0`（上滑）→ 进入 DETECTED，正常手势流程
      - 否则 → 标记为 REJECTED（非上滑手势）
    - ACTION_UP:
      - 若状态为 AWAITING_DIRECTION 或 REJECTED（即没有触发上滑）→ 调用 tap 重放
      - 若状态为 DETECTED → 正常执行手势结果
- 新增 `GestureState.AWAITING_DIRECTION` 和 `GestureState.REJECTED` 状态
- 新增 tap 重放回调 `onReplayTap: ((Float, Float) -> Unit)?`

### 3.4 GestureEngine.kt

- `createDetector()` 传入 `triggerMode` 和 `onReplayTap` 回调
- `onReplayTap` 实现：调用 `GestureAccessibilityService.getInstance()?.dispatchTap(x, y)`
- `applyConfigDiff()` 新增检测：底部高度或触发模式变化时重建底部 overlay

### 3.5 GestureAccessibilityService.kt（需确认路径）

- 新增公开方法 `fun dispatchTap(x: Float, y: Float)`：
  ```kotlin
  fun dispatchTap(x: Float, y: Float) {
      val path = Path().apply { moveTo(x, y) }
      val stroke = GestureDescription.StrokeDescription(path, 0, 1)
      val gesture = GestureDescription.Builder().addStroke(stroke).build()
      dispatchGesture(gesture, null, null)
  }
  ```

### 3.6 HomeViewModel.kt

- 新增方法：
  - `fun setBottomTriggerHeight(dp: Float)`
  - `fun setBottomTriggerMode(mode: BottomTriggerMode)`

### 3.7 SettingsScreen.kt

- 新增 UI 组件（在「底部手势」开关下方，仅当底部手势启用时显示）：
  1. **底部敏感区高度滑块**：Slider, 范围 20f..80f, step 由 Slider 连续值处理，显示当前值 "XXdp"
  2. **触发模式选择**：SegmentedButton 或两个 RadioButton，「轻触」/「滑动」

### 3.8 EdgeSensorView.kt

- 无需修改。事件消费逻辑由 `EdgeGestureDetector.onTouchEvent()` 的返回值控制，当前已正确传递。

### 3.9 OverlayWindowFactory.kt

- 无需修改。高度参数已由外部传入。

---

## 4. 新增的 DataStore Key

| Key 名称 | 类型 | 默认值 | 说明 |
|-----------|------|--------|------|
| `bottom_trigger_height_dp` | `Float` | `48f` | 底部触发区域高度（dp） |
| `bottom_trigger_mode` | `String` | `"TOUCH"` | 触发模式：TOUCH 或 SWIPE |

定义位置：`GestureConfig.companion`，使用 `floatPreferencesKey` 和 `stringPreferencesKey`。

---

## 5. UI 变更

在 `SettingsScreen` 的「底部手势」开关下方，新增折叠区域（`AnimatedVisibility`，当 `bottomEnabled == true` 时展开）：

```
┌─────────────────────────────┐
│ 底部手势              [开关] │
├─────────────────────────────┤
│ 触发区域高度                 │
│ 20dp ──────●────────── 80dp │
│            48dp              │
│                              │
│ 触发模式                     │
│ ○ 轻触  触碰即检测           │
│ ● 滑动  仅上滑触发，点击穿透  │
└─────────────────────────────┘
```

---

## 6. 修改顺序

1. **GestureConfig.kt** — 添加 `BottomTriggerMode` 枚举和新字段/Key
2. **OpenSwipeApp.kt** — DataStore 读写新 key，新增 update 方法
3. **GestureAccessibilityService.kt** — 添加 `dispatchTap()` 方法
4. **EdgeGestureDetector.kt** — 添加 SWIPE 模式逻辑（AWAITING_DIRECTION/REJECTED 状态、tap 重放回调）
5. **GestureEngine.kt** — 传递新参数、config diff 检测高度/模式变化
6. **HomeViewModel.kt** — 暴露新设置方法
7. **SettingsScreen.kt** — 添加滑块和模式选择 UI
8. 测试：轻触模式行为不变；滑动模式下点击可穿透、上滑正常触发
