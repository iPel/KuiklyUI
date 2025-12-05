# 能力全览

本页介绍 Kuikly Compose 的当前开发状态、已支持的 API 情况以及业务使用现状。

## 开发目标

Kuikly Compose 的长期目标是**完全支持标准 Jetpack Compose API**，以实现：

- 与官方 Compose API 100% 对齐
- 跨端一致的开发体验
- 无缝迁移官方 Compose 代码

当前我们正在朝着这个目标持续迭代，已支持标准 Compose API 约 90% 的功能。

## 已支持 API 概览

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

### Material3 组件

**大部分支持**，基于 Compose 1.7 的 Material3 能力做了对齐：

- **基础组件**：`Text`、`Button`、`Card`、`Surface`
- **页面结构**：`Scaffold`、`TopAppBar`、`TabRow`、`Tab`
- **表单输入**：`TextField`、`OutlinedTextField`、`Checkbox`、`Switch`、`Slider`
- **反馈组件**：`Snackbar`、`ModalBottomSheet`、`CircularProgressIndicator`、`LinearProgressIndicator`
- **其他**：`Divider`、`PullToRefreshContainer`

> 注意：部分 Material3 组件的某些参数可能暂时未完全支持，具体以实际表现为准。

### 状态管理

**完全支持**，直接使用官方 `androidx.compose.runtime.*`：

- `remember`、`mutableStateOf`、`derivedStateOf`
- `LaunchedEffect`、`DisposableEffect`、`SideEffect`
- `rememberCoroutineScope`、`rememberUpdatedState`
- 所有状态管理相关的 API

### 动画系统

**大部分支持**，包括常用动画 API：

- `AnimatedVisibility`、`AnimatedContent`
- `Crossfade`、`animateContentSize`
- `Transition`、`updateTransition`
- `animate*AsState` 系列 API

### 手势系统

**大部分支持**，包括常用手势修饰符：

- `clickable`、`combinedClickable`
- `draggable`、`scrollable`、`transformable`
- 手势冲突处理

### 其他功能

- **Canvas**：支持 `Canvas` 组件进行自定义绘制
- **Modifier**：支持大部分常用 Modifier
- **主题系统**：支持 `MaterialTheme` 和主题定制

## 业务使用现状

Kuikly Compose 已在多个业务场景中投入使用：

- **新项目/新模块**：推荐优先使用 Kuikly Compose
- **跨端需求**：需要同时支持 Android、iOS、Web、鸿蒙、小程序的项目
- **动态化需求**：需要运行时更新 UI 的场景

### 使用建议

- **完全对齐的 API**：可以放心使用，行为与官方 Compose 一致
- **部分支持的 API**：建议先进行小范围验证，确认满足需求后再大规模使用
- **遇到问题**：欢迎在 [GitHub Issues](https://github.com/Tencent-TDS/KuiklyUI/issues) 中反馈

## 动态化能力（Beta）

Kuikly Compose 支持动态化能力，目前处于 **Beta 版本**：

### 能力说明

- **热更新**：支持运行时更新 Compose UI
- **动态下发**：支持服务端下发 UI 配置
- **混合使用**：可以与自研 DSL 混合使用

### 使用限制

- 动态化能力仍在持续优化中，部分场景可能存在限制
- 建议在非关键路径先进行验证
- 如有问题请及时反馈

### 后续规划

- 持续完善动态化能力
- 提升稳定性和性能
- 提供更多动态化示例和最佳实践

## 版本信息

- **基于 Compose 版本**：Compose 1.7
- **Runtime**：直接使用官方 `androidx.compose.runtime.*`
- **支持平台**：Android、iOS、HarmonyOS、Web、小程序

## 反馈与贡献

如果你在使用过程中发现：

- API 支持问题
- 行为与官方 Compose 不一致
- 性能问题
- 其他问题或建议

欢迎通过以下方式反馈：

- [GitHub Issues](https://github.com/Tencent-TDS/KuiklyUI/issues)
- 内部反馈渠道（如有）

你的反馈将帮助我们持续改进 Kuikly Compose。

