# Material3 组件

本页说明 Kuikly Compose 中 Material3 组件的支持情况。基础用法请优先查阅 Jetpack Compose 官方文档。

> 官方文档（推荐起点）：[Material Design 3](https://m3.material.io/)  
> 详见：[Jetpack Compose 官方文档导航](./official-compose-links.md)

### 支持的组件

Kuikly 基于 Compose 1.7 的 Material3 能力做了对齐，常用组件包括：

#### 基础组件

- **Text** - 文本显示
- **Button** - 按钮
- **Card** - 卡片容器
- **Surface** - 表面容器

#### 页面结构

- **Scaffold** - 脚手架（带 TopBar、BottomBar）
- **TopAppBar** / **CenterAlignedTopAppBar** - 顶部导航栏
- **TabRow** / **ScrollableTabRow** - 标签栏
- **Tab** - 标签页

#### 表单与输入

- **TextField** / **OutlinedTextField** - 文本输入框
- **Checkbox** - 复选框
- **Switch** - 开关
- **Slider** / **RangeSlider** - 滑块

#### 反馈与弹层

- **Snackbar** / **SnackbarHost** - 消息提示
- **ModalBottomSheet** - 底部弹窗
- **CircularProgressIndicator** / **LinearProgressIndicator** - 进度条

#### 其他组件

- **HorizontalDivider** / **VerticalDivider** - 分割线
- **PullToRefreshContainer** - 下拉刷新容器

这些组件通常位于：

```kotlin
import com.tencent.kuikly.compose.material3.*
```

### 兼容性说明

**完全兼容的组件**：

- 绝大多数 Material3 组件在 Kuikly 中的 API 形态与行为都与 Jetpack Compose 一致
- 对于这类组件，我们在官网上不会重复参数表，而是直接给出官方链接
- 建议直接参考 Jetpack Compose 官方文档了解详细 API

**存在差异或局限的组件**：

- 某些平台上受限于原生控件实现，可能在细节上与 Android 官方实现略有差异
- 若有明显行为差异或暂不支持的属性，会在后续「组件差异清单」中明确标注

### 使用示例

```kotlin
@Composable
fun ExampleComponents() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "标题",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Button(onClick = { /* 处理点击 */ }) {
            Text("按钮")
        }
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("卡片内容")
            }
        }
        
        TextField(
            value = "",
            onValueChange = { /* 处理输入 */ },
            label = { Text("输入框") }
        )
    }
}
```

### 组件列表参考

详细的组件 API 列表，请查看：[组件列表](./components-list.md)

### 下一步

- 了解布局组件，请查看：[布局](./layout.md)
- 了解状态管理，请查看：[状态管理](./state-management.md)

