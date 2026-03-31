# Issue #4 实现计划：支持上下/左右滑动分别独立开关，设置实时生效

## 现状分析

- `GestureConfig` 是纯 data class，已有 `leftEnabled`/`rightEnabled`/`bottomEnabled` 字段，但无持久化
- `OpenSwipeApp` 持有一个静态的 `GestureConfig` 实例，Service 在 `onServiceConnected` 时读取一次
- `GestureEngine` 构造时接收不可变的 `config`，仅在 `onConfigurationChanged`（屏幕旋转）时才重建 overlay
- `HomeScreen` 的 Switch 状态是 `remember { mutableStateOf() }`，与 GestureEngine 完全断开
- DataStore Preferences 依赖已在 `build.gradle.kts` 中声明但未使用

## 一、需要修改的文件清单

### 1. `GestureConfig.kt`
- 保留 data class 定义不变（仍用作内存快照）
- 新增 companion object 定义三个 DataStore key 常量：`KEY_LEFT_ENABLED`、`KEY_RIGHT_ENABLED`、`KEY_BOTTOM_ENABLED`

### 2. `OpenSwipeApp.kt`
- 移除静态 `gestureConfig` 属性
- 创建 `DataStore<Preferences>` 单例（顶层 `val Context.settingsDataStore` 委托）
- 暴露 `gestureConfigFlow: Flow<GestureConfig>`，从 DataStore map 到 GestureConfig
- 提供 suspend 函数 `updateEdgeEnabled(edge: Edge, enabled: Boolean)` 写入 DataStore

### 3. `GestureEngine.kt`
- 构造参数从 `config: GestureConfig` 改为 `configFlow: StateFlow<GestureConfig>`
- `start()` 中启动协程 collect `configFlow`，对比新旧 config 的 enabled 字段差异
- 新增 `applyConfigDiff(old: GestureConfig, new: GestureConfig)` 方法：
  - 对每个 edge（LEFT/RIGHT/BOTTOM），若从 enabled→disabled，调用 `overlayManager.removeWindow()` 并移除 detector
  - 若从 disabled→enabled，创建对应 overlay window 和 detector
  - 其他字段变化（如 triggerWidth）暂忽略，未来可扩展
- `onConfigurationChanged` 仍全量重建，但基于最新 config 快照
- `stop()` 取消协程 scope

### 4. `GestureAccessibilityService.kt`
- `onServiceConnected` 中不再从 `OpenSwipeApp` 取静态 config
- 从 `OpenSwipeApp` 获取 `gestureConfigFlow`，转为 `StateFlow`（使用 `stateIn`），传给 GestureEngine
- 启动 Service 级 `CoroutineScope`（`lifecycleScope` 或自建 SupervisorJob scope），在 `cleanup()` 中取消

### 5. `HomeScreen.kt`
- 移除 `remember { mutableStateOf }` 的本地状态
- 接收 `HomeViewModel` 或直接接收 configFlow + 回调参数
- 三个独立 Switch：左边缘开关、右边缘开关、底部手势开关
- Switch 的 `checked` 绑定 DataStore 的实时值（collectAsState）
- Switch 的 `onCheckedChange` 调用 ViewModel/回调写入 DataStore
- 移除当前无用的灵敏度 Slider（或保留为 Phase 2 占位）

### 6. `MainActivity.kt`
- 如果引入 ViewModel：在 NavHost 的 `composable("home")` 中构造/获取 HomeViewModel，传给 HomeScreen
- 如果不引入 ViewModel：将 `OpenSwipeApp` 的 DataStore 方法通过 lambda 传递

## 二、需要新增的文件

### 1. `app/src/main/java/com/openswipe/ui/viewmodel/HomeViewModel.kt`（推荐）
- 注入 Application context，访问 DataStore
- 暴露 `configState: StateFlow<GestureConfig>`（来自 `OpenSwipeApp.gestureConfigFlow`）
- 提供 `setLeftEnabled(Boolean)`、`setRightEnabled(Boolean)`、`setBottomEnabled(Boolean)` 方法，内部调用 DataStore 写入

## 三、数据流设计

```
┌──────────┐   write    ┌────────────┐  Flow<Pref>  ┌──────────────┐
│ HomeScreen│ ────────→ │  DataStore  │ ──────────→ │ OpenSwipeApp │
│  Switch   │           │ Preferences │              │ .configFlow  │
└──────────┘            └────────────┘              └──────┬───────┘
                                                           │
                                                    map to GestureConfig
                                                           │
                                                    StateFlow<GestureConfig>
                                                           │
                                          ┌────────────────┴────────────────┐
                                          │                                 │
                                   ┌──────▼───────┐                 ┌──────▼──────┐
                                   │ HomeViewModel │                 │GestureEngine│
                                   │ (UI 展示用)   │                 │ (collect →  │
                                   └──────────────┘                 │  applyDiff) │
                                                                    └─────────────┘
```

### DataStore key 定义

```
key_left_enabled   → booleanPreferencesKey("edge_left_enabled"),   默认 true
key_right_enabled  → booleanPreferencesKey("edge_right_enabled"),  默认 true
key_bottom_enabled → booleanPreferencesKey("edge_bottom_enabled"), 默认 true
```

### StateFlow 链路

1. `Context.settingsDataStore.data`（冷 Flow）→ `map { prefs -> GestureConfig(...) }` → `stateIn(scope, SharingStarted.Eagerly, GestureConfig())`
2. 该 StateFlow 在 `OpenSwipeApp.onCreate()` 中用 application scope 启动
3. `GestureAccessibilityService` 和 `HomeViewModel` 共享同一个 StateFlow 实例

### Config 变更时 GestureEngine 动态响应

GestureEngine.start() 中：
```
configFlow.collect { newConfig ->
    val old = currentConfig
    currentConfig = newConfig
    applyConfigDiff(old, newConfig)
}
```

`applyConfigDiff` 逻辑：
- 遍历 `[LEFT, RIGHT, BOTTOM]`
- 用 helper 函数 `isEdgeEnabled(config, edge)` 获取 old/new 的 enabled 值
- 如果 `old=true, new=false`：`overlayManager.removeWindow("sensor_${edge.name.lowercase()}")` + `detectors.remove(edge)`
- 如果 `old=false, new=true`：创建 detector 和 overlay window（复用 `createOverlayWindows` 中单个 edge 的逻辑，抽取为 `addEdgeOverlay(edge)`）
- 如果相同：不操作

## 四、UI 变更

### HomeScreen 布局

保留现有结构，修改为：

1. **服务状态卡片** — 不变
2. **左边缘手势 Switch** — title: "左边缘手势", subtitle: "从左边缘向右滑动触发返回"
3. **右边缘手势 Switch** — title: "右边缘手势", subtitle: "从右边缘向左滑动触发返回"
4. **底部手势 Switch** — title: "底部手势", subtitle: "从底部上滑回到主页/最近任务"
5. 移除灵敏度 Slider（或保留为禁用/占位状态）

### Switch 写入 DataStore 流程

```
onCheckedChange = { enabled ->
    viewModel.setLeftEnabled(enabled)  // 内部: scope.launch { dataStore.edit { it[KEY] = enabled } }
}
```

DataStore 写入后自动触发 Flow 发射 → StateFlow 更新 → GestureEngine collect 到变更 → applyConfigDiff 增删 overlay

## 五、每个修改的具体代码变更描述

### GestureConfig.kt
- 在 companion object 中定义三个 `booleanPreferencesKey` 常量
- data class 字段保持不变

### OpenSwipeApp.kt
- 顶层声明 `val Context.settingsDataStore by preferencesDataStore(name = "settings")`
- 移除 `val gestureConfig: GestureConfig = GestureConfig()`
- 新增 `lateinit var gestureConfigFlow: StateFlow<GestureConfig>`
- 在 `onCreate()` 中初始化：将 `settingsDataStore.data.map { ... }` 转为 StateFlow（使用 `ProcessLifecycleOwner` 或 `GlobalScope`/application scope `stateIn`）
- 新增 `suspend fun updateEdgeEnabled(edge: Edge, enabled: Boolean)` — 根据 edge 选择对应 key 写入 DataStore

### GestureEngine.kt
- 构造参数 `config: GestureConfig` → `configFlow: StateFlow<GestureConfig>`
- 新增 `private var currentConfig: GestureConfig` 字段，初始值取 `configFlow.value`
- `start()` 内启动 `scope.launch { configFlow.collect { ... applyConfigDiff ... } }`，首次 collect 即触发 `createOverlayWindows()`
- 抽取 `addEdgeOverlay(edge: Edge)` 方法（从 `createOverlayWindows` 中提取单个 edge 的创建逻辑）
- `createOverlayWindows()` 改为调用三次 `addEdgeOverlay`，仍根据 `currentConfig` 的 enabled 判断
- 新增 `applyConfigDiff(old, new)`：对比三个 enabled 字段，按需调用 `addEdgeOverlay` 或 `removeEdge`
- 新增 `removeEdge(edge: Edge)`：`overlayManager.removeWindow(tag)` + `detectors.remove(edge)`
- `onConfigurationChanged` 改为使用 `currentConfig` 而非构造参数

### GestureAccessibilityService.kt
- 新增 `private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)`
- `onServiceConnected` 中：从 `(application as OpenSwipeApp).gestureConfigFlow` 获取 StateFlow，传给 GestureEngine 构造
- `cleanup()` 中取消 `serviceScope`

### HomeViewModel.kt（新增）
- 继承 `AndroidViewModel`
- 从 `application.settingsDataStore` 构建 configFlow 的只读 StateFlow（或直接引用 `OpenSwipeApp.gestureConfigFlow`）
- 三个方法：`setLeftEnabled`/`setRightEnabled`/`setBottomEnabled`，内部 `viewModelScope.launch` DataStore edit

### HomeScreen.kt
- 参数新增 `viewModel: HomeViewModel`
- `val config by viewModel.configState.collectAsState()`
- 替换三个 Switch 的 checked/onCheckedChange 绑定到 viewModel
- 移除 `remember { mutableStateOf/mutableFloatStateOf }` 的本地状态

### MainActivity.kt
- 在 `composable("home")` 中通过 `viewModel<HomeViewModel>()` 获取 ViewModel
- 传递给 `HomeScreen(viewModel = viewModel, ...)`

## 六、修改顺序（按依赖关系）

1. **GestureConfig.kt** — 添加 DataStore key 常量（无依赖）
2. **OpenSwipeApp.kt** — 初始化 DataStore + gestureConfigFlow（依赖 step 1）
3. **GestureEngine.kt** — 改为接收 StateFlow + 实现 applyConfigDiff（依赖 step 2）
4. **GestureAccessibilityService.kt** — 适配新的 GestureEngine 构造方式（依赖 step 3）
5. **HomeViewModel.kt** — 新建，连接 DataStore 读写（依赖 step 2）
6. **HomeScreen.kt** — 绑定 ViewModel（依赖 step 5）
7. **MainActivity.kt** — 注入 ViewModel（依赖 step 5, 6）

## 七、测试要点

### 功能测试
- [ ] 启动 app 后三个 Switch 默认均为开启状态（DataStore 无数据时的默认值）
- [ ] 关闭「左边缘手势」→ 左边缘 overlay 窗口立即被移除，左侧滑动不再触发返回
- [ ] 重新开启「左边缘手势」→ 左边缘 overlay 窗口立即被创建，左侧滑动恢复
- [ ] 同理测试右边缘和底部
- [ ] 三个开关可独立组合：如仅开启底部、仅开启左边缘等
- [ ] 杀进程重启后，开关状态保持（DataStore 持久化验证）

### 生命周期测试
- [ ] 屏幕旋转后 overlay 正确重建，且遵循当前开关状态
- [ ] Service 断开重连后，读取最新 DataStore 配置
- [ ] 在 Service 未连接时切换开关，连接后 overlay 按最新配置创建

### 边界测试
- [ ] 快速连续切换开关不会导致重复添加/移除 overlay（防抖/幂等性）
- [ ] 三个开关全部关闭时无 overlay 窗口残留
- [ ] 全部开启时三个 overlay 窗口均存在

### 性能测试
- [ ] DataStore 写入不阻塞 UI 线程（在 viewModelScope/IO 调度器中执行）
- [ ] StateFlow collect 在 Main 线程（overlay 操作需要在主线程）
