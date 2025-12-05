# 入门指南

本页帮助初学者了解应该阅读哪些文档来入门 Kuikly Compose，根据你的背景找到最适合的学习路径。

## 前置知识

在开始学习 Kuikly Compose 之前，建议先掌握以下基础知识：

### Kotlin
Kuikly Compose 使用 Kotlin 编写，你需要熟悉 Kotlin 的基础语法：
- [Kotlin 官方文档](https://kotlinlang.org/docs/home.html)

### Kotlin Multiplatform (KMP)
Kuikly Compose 基于 KMP 实现跨端，了解 KMP 有助于理解项目结构：
- [Kotlin Multiplatform 官方文档](https://kotlinlang.org/docs/multiplatform/kmp-overview.html)

**重点了解**：
- commonMain / platformMain 目录结构
- expect / actual 机制
- 平台特定实现

## Jetpack Compose 官方文档

Kuikly Compose 与官方 Jetpack Compose API 高度一致，**强烈建议先学习官方 Compose 文档**。

### Compose 入门必读

这些是理解 Compose 的核心概念，必须掌握：

1. **[Compose 编程思想](https://developer.android.com/develop/ui/compose/mental-model?hl=zh-cn)**
   - 理解声明式 UI 的思维方式
   - 了解重组（Recomposition）机制
   - 掌握状态驱动 UI 的理念

2. **[生命周期](https://developer.android.com/develop/ui/compose/lifecycle?hl=zh-cn)**
   - 理解 Compose 的生命周期
   - 了解初始组合、重组、离开组合
   - 掌握生命周期与状态的关系

3. **[状态管理](https://developer.android.com/develop/ui/compose/state)**
   - `remember`、`mutableStateOf` 的使用
   - 状态提升
   - 状态在重组中的行为

4. **[副作用](https://developer.android.com/develop/ui/compose/side-effects?hl=zh-cn)**
   - `LaunchedEffect`、`DisposableEffect` 的使用
   - 副作用的最佳实践
   - 理解副作用在重组中的行为

5. **[布局基础知识](https://developer.android.com/develop/ui/compose/layouts/basics)**
   - Column、Row、Box 的使用
   - Modifier 的链式组合
   - 布局测量与放置

### 推荐阅读

这些文档帮助你更深入理解 Compose，建议按需阅读：

1. **[为什么采用 Compose](https://developer.android.com/develop/ui/compose/why-adopt?hl=zh-cn)**
   - 了解 Compose 的优势
   - 理解与传统 View 系统的区别

2. **[界面架构](https://developer.android.com/develop/ui/compose/architecture)**
   - Compose 的架构设计
   - 生命周期
   - 副作用处理

3. **[CompositionLocal](https://developer.android.com/develop/ui/compose/compositionlocal?hl=zh-cn)**
   - 理解 CompositionLocal 的作用
   - 如何使用 CompositionLocal 传递隐式依赖
   - CompositionLocal 的最佳实践

4. **[列表](https://developer.android.com/develop/ui/compose/lists)**
   - LazyColumn、LazyRow 的使用
   - 列表性能优化

5. **[动画](https://developer.android.com/develop/ui/compose/animation/choose-api?hl=zh-cn)**
   - 动画 API 的使用

## 不同背景的学习路径

### 如果你已有 Jetpack Compose 经验

**推荐路径**：
1. [概述](./overview.md) - 快速了解 Kuikly Compose 与官方 Compose 的区别
2. [快速开始](./getting-started.md) - 了解包名规则和基本用法
3. [布局](./layout.md) 和 [组件](./components.md) - 查看对齐情况与差异
4. [多端开发](./multiplatform.md) - 了解跨端注意事项

**重点关注**：
- 包名切换（`androidx` → `com.tencent.kuikly.compose`）
- 与官方 API 的差异点
- 跨端开发注意事项

### 如果你是 iOS / Web / 前端开发者

**推荐路径**：
1. [概述](./overview.md) - 了解 Kuikly Compose 是什么
2. **先学习 Jetpack Compose 基础**（见上方"Compose 入门必读"）
3. [快速开始](./getting-started.md) - 创建第一个页面
4. 回到 Kuikly Compose 文档，学习跨端开发

**重点关注**：
- 先掌握 Compose 的基础概念（声明式 UI、状态管理）
- 理解 Kuikly 的跨端能力
- 多端差异与注意事项

### 如果你在使用 Kuikly 自研 DSL

**推荐路径**：
1. [概述](./overview.md) - 了解 Compose 与自研 DSL 的关系
2. **先学习 Jetpack Compose 基础**（见上方"Compose 入门必读"）
3. [快速开始](./getting-started.md) - 体验 Compose 的写法
4. [与 Core 集成](./interop-core.md) - 了解如何复用 Core 能力
5. 根据业务需求选择使用 Compose 或自研 DSL

**重点关注**：
- Compose 与自研 DSL 的适用场景
- 两者如何协同使用
- 迁移策略

### 重要提示

Kuikly Compose 文档**不重复** Jetpack Compose 的基础教程：

- **基础概念**（Composable、State、Recomposition 等）：请参考 Jetpack Compose 官方文档
- **组件 API 详情**：请参考 Jetpack Compose 官方文档
- **Kuikly文档重点**：差异点、注意事项
