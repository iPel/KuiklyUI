## Kuikly Compose 概览

### 适合谁阅读
- 已有 Jetpack Compose / Android 经验，想要一套 **跨 Android / iOS / Web / 鸿蒙 / 小程序** 的统一 UI 方案的同学
- iOS / Web / Harmony 等客户端或前端工程师，希望复用统一 DSL 与业务逻辑
- 正在使用 Kuikly 自研 DSL，希望评估/引入 Compose 方案的团队

### Kuikly Compose 是什么
一句话：**在 Kuikly Core 跨端引擎上，对齐 Jetpack Compose DSL 的跨端实现**，与自研 DSL 并行，业务可按场景选择。

- 基于官方 `androidx.compose.runtime`，完整复用 Compose 的 **状态与重组模型**
- 将 UI / Material / Foundation 等包迁移为 `com.tencent.kuikly.compose.*`
- 通过自定义 `KuiklyApplier` 将 Compose 节点映射到 Kuikly 原子组件，复用 Kuikly 的跨端渲染与动态化能力
- 最终渲染到各端原生视图：Android View、iOS UIKit、HarmonyOS ArkUI、Web DOM/WXML、小程序原生组件

> 和记忆方式：**“Compose 写法不变，渲染底座换成 Kuikly Core，多端可达”。**

### 与 Jetpack Compose 的关系
Kuikly Compose 的设计原则是：**最小侵入、最大复用**。

- Runtime：直接使用官方 `androidx.compose.runtime.*`，行为与 Jetpack Compose 保持一致
- UI/Material/Foundation：包名替换为 `com.tencent.kuikly.compose.*`，API 形态与官方尽量对齐
- 渲染层：不使用 AndroidComposeView/Skia，而是通过 `KuiklyApplier` 接入 Kuikly Core
- 平台支持：在 Jetpack Compose 官方 Android 基础上，扩展到 iOS / HarmonyOS / Web / 小程序

本官网 **不重复** Jetpack Compose 已有的概念/基础教程：

- 组件、布局、Modifier、状态管理、动画等基础知识，请优先参考官方文档
- 我们会在每个相关章节顶部给出推荐的 **Jetpack Compose 官方文档链接**
- 本站重点说明：**Kuikly 的差异点、跨端注意事项、与 Core 模块的协同方式**

### Kuikly Compose 的核心特性
- **跨端一致**：一套 Compose DSL，运行在 Android、iOS、HarmonyOS、Web、小程序
- **高度对齐官方 Compose**：组件 / Modifier / 布局 / 动画 等路径约 95% 对齐
- **动态化能力**：复用 Kuikly 的动态更新能力，支持运行时 DSL 与数据驱动渲染
- **原生渲染性能**：底层仍然是各端原生渲染栈，保留滚动、手势、动画的原生体验
- **生态与 AI 友好**：沿用标准 Compose 语义，更易复用社区最佳实践与通用 AI 工具

### 与 Kuikly 自研 DSL 的关系
Kuikly 当前存在两条 UI DSL：

- **Kuikly 自研 DSL**：更偏向 Schema / 配置化，适合动态化、低代码、Server Driven UI 等场景
- **Kuikly Compose**：对齐 Jetpack Compose，适合已有 Compose 经验的团队，以及需要强 IDE/AI 支持的场景

两者可以在同一应用中并存，按页面/模块逐步迁移或混用：

- 新项目/新模块：推荐优先使用 Kuikly Compose
- 需要强动态化/配置化的页面：可以继续使用自研 DSL，或编排 Compose 与 DSL 的混排方案

### 官方文档导航
如果你对 Jetpack Compose 还不熟悉，推荐先阅读官方文档（英文/中文任选）：

- 入门与基础概念：布局、状态、重组、Modifier、列表、动画等
- 官方教程建议阅读顺序与链接详见：[Jetpack Compose 官方文档导航](./official-compose-links.md)

当你对 Compose 有基本概念后，再回到 Kuikly Compose 文档，你会更容易理解我们在跨端与动态化层面的改造。

### 下一步推荐阅读
- [快速开始：5 分钟跑起第一个 Kuikly Compose 页面](./getting-started.md)
- [核心概念：KuiklyApplier、ComposeContainer 与跨端模型](./concepts.md)
- [布局与组件：与 Jetpack Compose 的对齐与差异](./layout-components.md)


