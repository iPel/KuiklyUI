## Kuikly Compose 架构演进与落地

> 你可以直接在此文档上批注/修改，我会根据你的意见再细化。

### 1. Kuikly Compose 架构演进与落地
- 团队 / 演讲者 / 日期

### 1. Kuikly Compose 是什么
左图、右边定义； 

一句话回答：Kuikly Compose 是在 Kuikly Core 上扩展原生支持标准 Compose DSL，与自研 DSL 并行，业务可自选的高阶 UI 框架。

ppt上写（精炼点）
  - 定义：在 Kuikly 引擎之上对齐 Jetpack Compose DSL 的跨端实现
  - 标准对齐：≈95% Compose API 对齐（组件/Modifier/布局/动画）
  - 动态化：运行时 DSL 与数据驱动渲染，支持远程下发与热更新
  - 原生渲染：沿用 Kuikly 各端原生渲染栈，性能与交互一致
  - 多端扩容：覆盖 Android/iOS/Web/鸿蒙/小程序 等官方未覆盖平台

分享口语：
  - Kuikly 已有一套自研 DSL；本项目是在不动 Kuikly 渲染架构的前提下，引入并对齐 Compose DSL，让“会 Compose 就会 Kuikly”。
  - 我们做到约 95% API 对齐，重点覆盖组件、布局（Measure/Place）、Modifier、动画与手势等核心路径。
  - 支持动态化：服务端下发/配置驱动 → 端侧重组渲染，配合灰度与热更新能力。
  - 保持原生渲染栈（非 WebView），确保帧率、滚动/手势与交互一致性。
  - 扩容至 Compose 官方未覆盖的平台（如鸿蒙/小程序），一套 DSL，多端可达。

### 2. 兼容 Compose DSL：动机与价值
一句话观点：用主流 DSL 解锁 AI/生态/工具链红利，让 Kuikly 的跨端能力被更多人更快用起来。

ppt上写（精炼点）
  - AI友好：标准语义，Prompt→代码命中率更高
  - 零门槛：Compose 开发者即上手，培训与试错成本低
  - 生态扩展：借助 Compose 开源生态，快速扩充 Kuikly 版图
  - 高阶能力直达：SubcomposeLayout 分区测量、Lazy 超大列表与稳定重组、统一手势/nestedScroll、物理与状态动画、AndroidView/ComposeView 互操作
  - 工具链：Preview/Inspection/Profiler 即插即用，排障更快

分享口语： 
  - AI 友好：标准 Compose 语义更易被大模型“直译”与生成，Prompt→代码成功率更高
  - 受众广：大量 Android/Compose 开发者可几乎零迁移成本上手，降低培训与试错成本
  - 生态扩展：自研 DSL 生态建设成本高；Compose 开源生态完备，借 Compose DSL 扩充 Kuikly 生态与边界，少造轮子
  - 高阶能力直达：SubcomposeLayout 分区测量、Lazy 超大列表与稳定重组、统一手势/nestedScroll、物理与状态动画、AndroidView/ComposeView 互操作
  - 工具链与可观测：预置的 Preview、Inspection、Profiler 等工具即插即用，排障效率提升
  
// 生态不光是组件，还有业界的最佳实践，经验。 

### 3. Kuikly Compose 架构思考：最小侵入、最大复用
- 一句话问题：Compose 能力丰富，如何在不牺牲 Kuikly 原生渲染与动态化的前提下完整对接？
- 难点总览：
  - 渲染对接：自定义 Applier 同构两棵树，复用 Runtime/状态，增量映射节点操作
  - 布局对接：Flex/Constraint → Measure/Place；基线/Intrinsics 一致
  - 动画与手势：时序对齐、可中断语义、nestedScroll 冲突治理
  - 滚动体系：嵌套滚动、Overscroll、分页、父子联动
  - 动态化与性能：最小重组、懒加载、资源复用、异步合帧
  - 跨端一致性与工具链：滚动/回弹/字体/密度一致；Preview/Profiler/日志

（后续小节将按此顺序展开）

结论先行：
  - 选型：保留 Kuikly 原生渲染与动态化（不动底座），嫁接标准 Compose DSL
  - 对接方式：自定义 Applier 同构两棵树，完整复用 Compose Runtime/状态
  - 对齐目标：≈95% Compose API（组件/Modifier/布局/动画/手势）
  - 覆盖平台：Android/iOS/Web/鸿蒙/小程序

设计原则：
  - 最小侵入、可回退：不替换渲染引擎，关键路径可关闭/降级
  - 一致性优先：布局/滚动/动画/事件时序以 Kuikly 基线为准
  - 可观测：日志/埋点/Profiler 贯通，问题可定位可量化
  - 可演进：双栈并行（自研 DSL/Compose DSL），支持渐进式迁移

### 3.1 架构融合：Kuikly 底座，升级 Compose 开发体验
- 引擎层、DSL 解析、适配层、平台渲染层、桥接
- 一图总览模块边界与数据流向

一句话观点：渲染与动态，嫁接 Compose DSL 与生态——最小侵入、最大复用。

ppt上写（精炼点）
  - 左：Kuikly（DSL/解析/动态化/原生渲染）｜右：Compose（Runtime/Layout/Modifier）
  - 融合三路：A Render替换｜B 保留Kuikly渲染+Compose DSL｜C 双栈混排
  - 选择B：保留渲染与动态化，引入 Compose 能力与生态

左右架构示意（建议配对比图）
  - 左｜Kuikly 架构
    - DSL/Schema → 解析器 → 虚拟树/状态管理 → 布局/渲染引擎 → 平台渲染(Android/iOS/Web/鸿蒙/小程序)
    - 动态化：远程下发/灰度/热更新/可观测（日志/埋点/Profile）
  - 右｜Compose 架构
    - Composable → Composition/Recomposer/Snapshot → Measure/Place → Modifier 链 → 平台 View
    - 能力域：手势/滚动（nestedScroll）、动画（状态/物理）、互操作（AndroidView/ComposeView）

三种融合思路（取舍对比）
  - 方案A｜Compose 渲染主导
    - 做法：以 Compose 渲染栈为核心，Kuikly 作为宿主与桥接
    - 优点：最大程度复用 Compose 运行时与工具链
    - 风险：跨端一致性与非 Android 平台渲染成本高，动态化能力需重搭
  - 方案B｜Kuikly 渲染主导 + Compose DSL 前置（选定）
    - 做法：保留 Kuikly 原生渲染与动态化；新增 Compose DSL 映射与适配层，完成 API 对齐
    - 优点：延续 Kuikly 的跨端渲染/动态化优势，同时获取 Compose 的 DSL/组件/工具链能力
    - 风险：适配层复杂度较高（布局/滚动/动画/手势的一致性对齐）
  - 方案C｜双栈混排/渐进迁移
    - 做法：按页面/组件选择 Kuikly 自研 DSL 或 Compose DSL，分阶段替换与共存
    - 优点：风险可控，迁移友好
    - 风险：双栈维护成本、边界与互操作复杂度

结论
  - 选择方案B：在保留 Kuikly 原生渲染与动态化的前提下，引入并对齐 Compose 能力，实现“跨端一致 + 生态增强”。

### 4. 渲染对接｜基于自定义 Applier 同构两棵树
一句话观点：自定义 Applier 将 Compose 的增量变更映射为 Kuikly 节点操作，完整复用 Compose Runtime 与状态管理。

图注（精炼放在图片旁）
  - 入口：业务代码（标准 Compose DSL）→ 编译器插桩 → Runtime 重组 → 布局引擎
  - Applier：捕获组合/重组，分发 insert/remove/move/update 四类操作
  - 映射：布局属性(position/size)；通用绘制(bgColor/border/shader)；组件专有(font/text/onLayout)
  - 输出：生成 KNode 树 → Kuikly Core → 各平台 Render（Android/iOS/Web/鸿蒙/小程序）
  - 价值：复用 Compose Runtime/状态，最小变更；原生交互/性能保持一致
  - 边界：复杂绘制走原子组件与平台 Render，避免跨端差异

ppt上写（精炼点）
  - 自定义 Applier：让 Compose 树与 Kuikly 虚拟树“同构”
  - 复用 Compose Runtime/Recomposer/Snapshot，状态与重组全走 Compose
  - 节点操作映射：insert/remove/move/update → Kuikly 节点与属性/事件
  - 事件回流：Kuikly 原生事件 → Compose 状态/副作用（Launched/Disposable）
  - 动态化：DSL 下发 → 生成 Composable → Applier 驱动平台渲染

分享口语：
  - 我们没有改动 Compose 的 Runtime，而是实现了自定义 Applier，把 Compose 的“增量变更”翻译成 Kuikly 的节点/属性操作，从而“同构”两棵树。
  - Compose 负责状态、快照与重组；Kuikly 负责跨端的布局/渲染与平台一致性，二者职责清晰、各扬所长。
  - insert/remove/move/update 等操作被映射到 Kuikly 的创建/销毁/重排/属性设置，事件从 Kuikly 回流到 Compose，触发状态与副作用链。
  - 动态化场景下，服务端下发/配置变化只会触发必要的重组与最小变更，性能与一致性都可控。

### 5. 布局对接｜布局系统映射与约束恢复
- Flex/Constraint → Compose Layout 的映射
- 测量/放置、基线、intrinsics、一致性

### 6. 技术难点｜滚动体系对接与嵌套联动
- 嵌套滚动、Overscroll、分页、父子联动
- 手势冲突与事件一致性策略

### 7. 技术难点｜动画、手势与事件一致性
- 动画可中断与时序对齐
- 事件链：拦截/冒泡/竞争处理

### 8. 技术难点③｜动态化与性能优化
- 重组最小化、跳过策略、懒加载、资源复用
- 异步 diff、合并事务、绘制避免抖动

### 9. 业务落地案例｜关键页面/组件前后对比
- 指标对比图 + 代码/人天对比
- 业务反馈与风险收敛

### 10. Roadmap｜下一步计划与社区合作
- 组件完备度、并行渲染、更多平台
- 开源与生态协作

一句话观点：补齐组件、增强并行渲染，继续扩容平台并拥抱社区。

---

### 备份页（可选，放隐藏/附录）
能力覆盖简表（组件/Modifier/动画/手势/布局，标注已对齐项）。
滚动冲突处理策略（父子优先级矩阵）。
风险与边界（一句话列点）。
