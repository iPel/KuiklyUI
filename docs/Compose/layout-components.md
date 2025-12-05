# 布局与组件：对齐与差异概览

本页只关注 **Kuikly 与 Jetpack Compose 的对齐情况和差异点**，基础用法请优先查阅 Jetpack Compose 官方文档。

> 官方文档（推荐起点）：布局 / 组件 / Modifier / 列表 / 动画  
> 详见：[Jetpack Compose 官方文档导航](./official-compose-links.md)

### 布局组件对齐情况

Kuikly 当前重点支持以下布局组件，并与 Jetpack Compose 对齐：

- 布局容器：
  - `Column` / `Row` / `Box`
  - `BoxWithConstraints`
- 流式布局：
  - `FlowRow` / `FlowColumn`
- 辅助组件：
  - `Spacer`

在 Kuikly 中它们都位于：

- `com.tencent.kuikly.compose.foundation.layout.*`

对大多数业务场景，这些布局的行为与 Jetpack Compose 保持一致：

- 测量与放置规则一致
- Modifier 链（`padding`、`size`、`fillMaxWidth` 等）语义一致

我们会在后续章节中逐步补充「在跨端场景下需要额外注意的点」，例如：

- 不同平台的密度 / 字体渲染差异
- 某些平台上极端约束下的表现差异

### Material3 组件支持情况

Kuikly 基于 Compose 1.7 的 Material3 能力做了对齐，常用组件包括：

- 基础组件：
  - `Text`、`Button`、`Card`、`Surface`
- 页面结构：
  - `Scaffold`、`TopAppBar` / `CenterAlignedTopAppBar`
  - `TabRow` / `ScrollableTabRow`、`Tab`
- 表单与输入：
  - `TextField` / `OutlinedTextField`
  - `Checkbox`、`Switch`、`Slider` / `RangeSlider`
- 反馈与弹层：
  - `Snackbar` / `SnackbarHost`
  - `ModalBottomSheet`
  - `CircularProgressIndicator` / `LinearProgressIndicator`

这些组件通常位于：

- `com.tencent.kuikly.compose.material3.*`

**完全兼容的组件**：

- 绝大多数 Material3 组件在 Kuikly 中的 API 形态与行为都与 Jetpack Compose 一致
- 对于这类组件，我们在官网上不会重复参数表，而是直接给出官方链接

**存在差异或局限的组件**（后续会单独列小节）：

- 某些平台上受限于原生控件实现，可能在细节上与 Android 官方实现略有差异
- 若有明显行为差异或暂不支持的属性，会在后续「组件差异清单」中明确标注

### 列表与滚动（List & Scroll）

列表和滚动是跨端实现中最容易出现差异的部分，因此我们单独强调：

- 支持的 Compose 列表组件：
  - `LazyColumn` / `LazyRow`
  - `LazyVerticalGrid` / `LazyHorizontalGrid`
  - `LazyVerticalStaggeredGrid` / `LazyHorizontalStaggeredGrid`
- 支持的 Pager：
  - `HorizontalPager` / `VerticalPager`

在 Kuikly 中，它们的渲染底层会落到各端的原子列表组件上，例如 Android 端的 `KRRecyclerView`：

- 支持分页滚动、回弹（bounces）、父子联动滚动、嵌套滚动冲突处理等能力
- 支持滚动事件回调、拖拽开始/结束、fling 结束等事件

后续我们会在「列表与滚动」专题中补充：

- 常见场景的推荐写法（长列表、吸顶、Tab + Pager 联动等）
- 各端在滚动物理与回弹效果上的差异说明
- 性能与可用性建议（例如占位、预取、局部刷新等）

### 动画与手势

Kuikly 支持 Compose 官方提供的大部分动画与手势 API，例如：

- 动画：
  - `AnimatedVisibility`、`AnimatedContent`
  - `Crossfade`、`animateContentSize`
  - `Transition` / `updateTransition`
- 手势与输入：
  - 常用的点击 / 长按 / 滑动等手势 Modifier

这些能力的语义与官方 API 基本保持一致，主要差异集中在：

- 不同平台的物理模拟参数（阻尼、加速度等）
- 极端场景下的性能表现

后续会在「动画与手势」章节中补充跨端差异与最佳实践。

### 推荐使用方式

- **基础概念与 API 详情**：直接查 Jetpack Compose 官方文档
- **Kuikly 相关差异与注意事项**：在本官网查阅对应专题：
  - [布局与组件专题（计划补充细节）](./layout-components.md)
  - 列表与滚动专题（计划新增）
  - 动画与手势专题（计划新增）

随着 Kuikly Compose 能力的演进，我们会持续维护一份「组件与布局对齐状态表」，帮助你快速判断某个官方能力在 Kuikly 中的支持状态。


