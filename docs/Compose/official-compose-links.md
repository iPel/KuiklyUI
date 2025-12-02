## Jetpack Compose 官方文档导航（推荐阅读顺序）

Kuikly Compose 在概念与语义上高度对齐 Jetpack Compose，因此基础用法推荐直接阅读官方文档。本页给出一条面向 **Kuikly 使用者** 的阅读路线建议。

> 下面所有链接请以 Jetpack 官方站点为准，这里只给出推荐入口与顺序，具体 URL 可在官方站点中按标题搜索获取最新版本。

### 1. 核心概念

- What is Jetpack Compose
- Thinking in Compose（如何用声明式方式思考 UI）
- State and recomposition（状态与重组）

掌握目标：

- 理解 `@Composable` 的含义
- 理解「状态驱动 UI」与「重组」的基本机制

### 2. 布局与 Modifier

- Layouts in Compose（Column / Row / Box 等）
- Modifiers（padding / size / fill / weight 等）

掌握目标：

- 会用基础布局构建简单页面
- 理解 Modifier 链式组合的思想

### 3. 列表与滚动

- Lazy lists（LazyColumn / LazyRow）
- Grids / Staggered grids / Pager（如有）

掌握目标：

- 能够构建常见列表页面（Feed 列表、宫格、轮播等）
- 了解列表项复用与性能的基本概念

### 4. 状态管理进阶

- State in Compose（官方进阶篇）
- Side-effects in Compose（LaunchedEffect / rememberCoroutineScope 等）

掌握目标：

- 在更复杂的业务下合理组织状态与副作用
- 掌握常见状态提升模式

### 5. 动画

- Animation in Compose（概览）
- High-level animations（AnimatedVisibility / Crossfade / animate*AsState 等）
- Transitions（updateTransition 等）

掌握目标：

- 能够为页面和组件添加基础交互动效
- 了解可中断动画与过渡动画的基本用法

### 6. 性能与最佳实践

- Performance best practices for Compose
- Thinking about recomposition and stability

掌握目标：

- 理解哪些写法可能导致不必要的重组
- 了解列表与复杂页面的性能优化思路

### 7. 更多专题（按需查阅）

- Accessibility in Compose
- Testing in Compose
- Interoperability（与 View / UIKit 等互操作，部分思路可借鉴到 Kuikly 场景）

---

在阅读官方文档的过程中，你可以随时对照 Kuikly Compose 文档中的对应章节：

- 如果只涉及 **纯 Compose 语义**，通常可以直接套用到 Kuikly 中
- 如果涉及 **具体平台视图 / 渲染细节**，再回来看 Kuikly 的「差异说明」与跨端适配章节


