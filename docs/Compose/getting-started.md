## 快速开始 Kuikly Compose

本页目标：**5 分钟内在工程中跑起第一个 Kuikly Compose 页面**，并搞清楚「与官方 Jetpack Compose 有哪些关键不同」。

> 前置假设：你已经按照 Kuikly 的整体 QuickStart（`/QuickStart`）完成了基础环境搭建。

### 1. 添加依赖

Kuikly Compose 作为 Kuikly 的一个模块提供，一般不需要你单独管理 Compose 版本：

- Kuikly 内部已经固定并对齐一套 Compose 版本（当前基于 Compose 1.7）
- 业务工程只需要按照 Kuikly 官方 QuickStart 指南引入依赖即可

详细依赖和版本信息，请参考：

- Kuikly 仓库根目录的 `compose` 模块 `build.gradle.kts`
- 官网文档中的依赖说明（TODO：链接到正式依赖文档）

### 2. 包名规则与导包方式

**Runtime 保持官方包名**：

- 继续使用官方 `androidx.compose.runtime.*`
- 包括 `@Composable`、`remember`、`mutableStateOf` 等 API，完全复用官方行为

**UI / Foundation / Material 等改为 Kuikly 包名**：

- 不再使用 `androidx.compose.foundation.*` / `androidx.compose.material3.*` / `androidx.compose.ui.*`
- 而是改为：
  - `com.tencent.kuikly.compose.foundation.*`
  - `com.tencent.kuikly.compose.material3.*`
  - `com.tencent.kuikly.compose.ui.*`

示例：

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import com.tencent.kuikly.compose.material3.Button
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.padding
```

> 记忆方式：**只有 runtime 还是 `androidx.compose.runtime`，其它 UI 相关包全部切到 `com.tencent.kuikly.compose`。**

### 3. 第一个 Kuikly Compose 页面

Kuikly 中的页面一般继承自 `ComposeContainer`（一个基于 Kuikly Core 的跨端页面容器），并在其中通过 `setContent` 设置 `@Composable` 内容。

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.tencent.kuikly.compose.material3.Button
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.ui.Modifier

// 伪代码，仅展示结构
class HelloComposePage : ComposeContainer() {

    override fun onCreate() {
        super.onCreate()

        setContent {
            HelloComposeScreen()
        }
    }
}

@Composable
private fun HelloComposeScreen() {
    val count = remember { mutableStateOf(0) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Hello Kuikly Compose, count = ${count.value}")
        Button(onClick = { count.value++ }) {
            Text(text = "Click me")
        }
    }
}
```

注意几点：

- `HelloComposePage` 继承自 Kuikly 的 `ComposeContainer`，而不是 Android Activity
- `setContent {}` 的用法与 Jetpack Compose 基本一致，但底层渲染通过 `KuiklyApplier` 连接到 Kuikly Core
- 页面生命周期、路由跳转等能力来自 Kuikly Core，与自研 DSL 页面保持一致

### 4. 多端运行示意

当你在 Kuikly 的 KMP 工程中编写上面的 `@Composable` 页面时：

- `commonMain` 中的 Compose 代码会被 Android / iOS / Harmony / Web / 小程序 共用
- 各端通过自己对应的 Render 模块负责真实视图的构建与事件分发

Illustration（简化流程）：

1. 业务代码调用 `setContent { HelloComposeScreen() }`
2. 官方 Compose Runtime 负责重组与状态管理
3. `KuiklyApplier` 将 Compose 树的增量变更映射为 Kuikly KNode 树操作
4. Kuikly Core 依据 KNode 驱动各端原生视图渲染

你仍然用 **Compose 的写法** 描述 UI，但渲染栈变成了 Kuikly 的跨端底座。

### 5. 官方 Compose 教程推荐阅读顺序

对于还不太熟悉 Jetpack Compose 的同学，建议先按官方路线补一遍基础：

1. 基础：Composable、State、Recomposition
2. 布局：Column/Row/Box 与 Modifier
3. 列表：LazyColumn / LazyRow / Grid
4. 动画：`animate*AsState`、`AnimatedVisibility`、`updateTransition`
5. 性能与最佳实践

我们在文档中提供了一页整理好的「Jetpack Compose 官方文档导航」，见：

- [Jetpack Compose 官方文档导航](./official-compose-links.md)

### 6. 下一步

- 想理解 Kuikly 在架构上的改造，请看：[核心概念与架构](./concepts.md)
- 想知道哪些组件/布局完全对齐官方，哪些有差异，请看：[布局与组件](./layout-components.md)


