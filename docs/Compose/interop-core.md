## 与 Core 模块协同（Module / Router / 动态化）

本页目标：说明在 Kuikly Compose 中，如何与 Kuikly Core 提供的 **Module / Router / 动态化能力** 协同工作。

> 详细的 Module / Router 设计与使用，请参考现有 Kuikly Core 文档，这里只从 Compose 视角给出用法与最佳实践。

### 推荐的分层方式

在使用 Kuikly Compose 时，我们推荐继续保持「**UI 与业务逻辑分层**」：

- **Compose 层**：负责声明式描述界面、状态与交互
- **Core Module 层**：负责跨端业务能力，如网络请求、存储、埋点、路由等

典型做法：

- 在 `@Composable` 中读取状态与调用模块提供的接口
- 将模块暴露出的挂起函数 / 回调转化为 Compose 友好的状态（如 `State` / `Flow` → `collectAsState`）

### 在 Compose 中使用 Router

Kuikly 的 Router 模块统一了多端的路由跳转，你可以在 Compose 页面中通过 Module 调用进行跳转。

伪代码示例（示意 API，具体以 Core 文档为准）：

```kotlin
@Composable
fun UserProfileScreen(userId: String) {
    val router = rememberKuiklyRouter()

    Button(onClick = {
        router.push("/user/edit", params = mapOf("id" to userId))
    }) {
        Text("编辑资料")
    }
}
```

你可以把 Router 当成一个「普通的跨端服务」，区别只是它提供的是 **页面级能力**。

### 在 Compose 中使用网络 / 存储等模块

对于网络、存储等常规模块，我们推荐：

- 尽量在 ViewModel / 业务层调用 Module，Compose 层只订阅结果
- 或在小型页面中直接通过 `LaunchedEffect` / `rememberCoroutineScope` 调用 Module

伪代码示例（示意 API，具体以 Core 文档为准）：

```kotlin
@Composable
fun ArticleListScreen() {
    val network = rememberKuiklyNetwork()
    val scope = rememberCoroutineScope()
    val articlesState = remember { mutableStateOf<List<Article>>(emptyList()) }

    LaunchedEffect(Unit) {
        val result = network.get("/api/articles")
        articlesState.value = result.items
    }

    ArticleListView(articles = articlesState.value)
}
```

> 更系统的 Module 使用方式与能力说明，请参阅：`/API/modules/overview.md` 及相关文档。

### 动态化与 Compose

Kuikly 的动态化能力允许服务端通过 DSL / 配置下发页面结构与数据，在 Compose 场景下可以有两种典型结合方式：

1. **DSL 驱动 UI，Compose 包裹逻辑**  
   - 核心 UI 结构使用自研 DSL 描述，由服务端下发  
   - Compose 层负责包裹 DSL View、编排状态与交互逻辑

2. **Compose 驱动 UI，服务端下发配置**  
   - 主要 UI 结构直接用 Compose 编写  
   - 服务端下发配置（布局参数、展示策略、AB 分流等），在 Compose 中通过状态驱动切换

无论选择哪种组合方式，推荐遵循以下原则：

- 尽量让 Compose 负责「**如何展示**」，让 Module / 动态化负责「**展示什么**」
- 将跨端能力封装为稳定的接口，Compose 只关心接口与数据，不关心实现细节

### 下一步

- 想要了解 Core Module 更完整的能力与 API，请阅读：`/API/modules/overview.md`
- 想看更完整的迁移与架构案例，请看：[迁移与对比](./migration-and-comparison.md)（计划补充）


