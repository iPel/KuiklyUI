# 能力全览

本页介绍 Kuikly Compose 的当前开发状态、已支持的 API 情况。

## 当前可用性
- **内置模式**：已在腾讯新闻、地图、IMA 等多个业务，超过 100 个页面线上验证，成熟可直接接入使用。
- **动态化模式（Beta）**：目前在业务灰度验证中，感兴趣的可联系试用接入。

## 定位与原则

- **与官方 Compose 对齐 API**：保持 API 形态和行为一致（当前约 95%，持续演进），便于直接迁移和使用官方生态。
- **AI 辅助编码友好**：因 API 高度对齐，可直接使用 Cursor / Copilot 等 AI 生成 Compose 代码。
- **跨端一致性与性能**：确保 Android / iOS / HarmonyOS / Web / 小程序一致的交互和性能体验。
- **差异化与扩展能力**：在对齐的基础上，扩展动态化、跨端特性及与 Kuikly Core 的深度协同，提供超出官方 Compose 的能力。

## 标准Compose API支持概览

### 状态管理

**完全支持**，直接使用官方 `androidx.compose.runtime.*`：

- `remember`、`mutableStateOf`、`derivedStateOf`
- `LaunchedEffect`、`DisposableEffect`、`SideEffect`
- `rememberCoroutineScope`、`rememberUpdatedState`
- 所有状态管理相关的 API


### 布局系统

**完全支持**，包括所有布局组件和布局修饰符：

- **基础布局**：`Column`、`Row`、`Box`、`BoxWithConstraints`
- **流式布局**：`FlowRow`、`FlowColumn`
- **自定义布局**：`Layout` 组件
- **布局修饰符**：`padding`、`size`、`fillMaxWidth`、`fillMaxHeight`、`fillMaxSize`、`weight` 等

### 列表与滚动

**完全支持**，包括所有列表组件：

- **列表**：`LazyColumn`、`LazyRow`
- **网格**：`LazyVerticalGrid`、`LazyHorizontalGrid`
- **瀑布流**：`LazyVerticalStaggeredGrid`、`LazyHorizontalStaggeredGrid`
- **轮播**：`HorizontalPager`、`VerticalPager`

### 动画系统

**完全支持**，包括常用动画 API：

- `AnimatedVisibility`、`AnimatedContent`
- `Crossfade`、`animateContentSize`
- `Transition`、`updateTransition`
- `animate*AsState` 系列 API

### 手势系统

**大部分支持**，包括常用手势修饰符：

- 点击与长按：`clickable`、`combinedClickable`
- 拖拽：`draggable`
- 变换手势：`transformable`
- 自定义手势：`pointerInput`

### Material3 组件

**大部分支持**，基于 Compose 1.7 的 Material3 能力做了对齐：  
- **基础组件**：`Text`、`Button`、`Card`、`Surface`  
- **页面结构**：`Scaffold`、`TopAppBar` / `CenterAlignedTopAppBar`、`TabRow` / `ScrollableTabRow`、`Tab`  
- **表单输入**：`TextField`、`Checkbox`、`Switch`、`Slider` / `RangeSlider`  
- **反馈组件**：`Snackbar` / `SnackbarHost`、`ModalBottomSheet`、`CircularProgressIndicator` / `LinearProgressIndicator`  
- **其他**：`HorizontalDivider` / `VerticalDivider`、`PullToRefresh`

### 其他功能

- **Canvas**：支持 `Canvas` 组件进行自定义绘制
- **Modifier**：支持大部分常用 Modifier
- **ViewModel & Lifecycle**：支持 `viewModel()`、生命周期感知的副作用与状态管理（与官方 Compose Runtime 对齐）

## 工具与调试

- **资源管理**：与官方能力对齐，支持常规资源加载与管理
- **预览**：建设中
- **Inspector**：可使用各端原生 Inspector（Android / iOS / HarmonyOS）
- **性能工具**：建设中

## 示例代码

我们提供了丰富的 Demo 示例，涵盖组件使用、手势交互、动画效果及列表滚动等核心场景。

- **代码路径**：[demo/src/commonMain/kotlin/com/tencent/kuikly/demo/pages/compose/](https://github.com/Tencent-TDS/KuiklyUI/tree/main/demo/src/commonMain/kotlin/com/tencent/kuikly/demo/pages/compose)
- **特别说明**：所有示例代码均由 AI 直接生成，无需手工调整即可运行。这验证了 Kuikly Compose 的 AI 辅助生成代码目前已处于高可用状态。

## 反馈与贡献
如果你在使用过程中发现：

- API 支持问题
- 行为与官方 Compose 不一致
- 性能问题
- 其他问题或建议

欢迎通过以下方式反馈：

- [GitHub Issues](https://github.com/Tencent-TDS/KuiklyUI/issues)
- 内部反馈渠道（端框架小助手）

你的反馈将帮助我们持续改进 Kuikly Compose。

