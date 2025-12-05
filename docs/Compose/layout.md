# 布局组件

本页说明 Kuikly Compose 中布局组件的支持情况与使用注意事项。基础用法请优先查阅 Jetpack Compose 官方文档。

> 官方文档（推荐起点）：[Layouts in Compose](https://developer.android.com/jetpack/compose/layouts)  
> 详见：[Jetpack Compose 官方文档导航](./official-compose-links.md)

### 支持的布局组件

Kuikly 当前重点支持以下布局组件，并与 Jetpack Compose 对齐：

#### 基础布局容器

- **Column** - 垂直排列子元素
- **Row** - 水平排列子元素
- **Box** - 层叠排列子元素
- **BoxWithConstraints** - 带约束的层叠布局

#### 流式布局

- **FlowRow** - 流式水平布局，自动换行
- **FlowColumn** - 流式垂直布局，自动换列

#### 辅助组件

- **Spacer** - 空白占位符

在 Kuikly 中它们都位于：

```kotlin
import com.tencent.kuikly.compose.foundation.layout.*
```

### 布局行为对齐

对大多数业务场景，这些布局的行为与 Jetpack Compose 保持一致：

- 测量与放置规则一致
- Modifier 链（`padding`、`size`、`fillMaxWidth` 等）语义一致
- 布局参数（`Arrangement`、`Alignment` 等）行为一致

### 跨端注意事项

在跨端场景下需要额外注意的点：

1. **不同平台的密度 / 字体渲染差异**
   - 各平台默认字体不同，可能导致文本测量结果略有差异
   - 建议使用 `dp` 单位而非 `sp` 进行布局，避免字体大小影响布局

2. **极端约束下的表现差异**
   - 某些平台上在极端约束（如极小宽度）下可能出现布局差异
   - 建议在关键布局处进行多端真机验证

3. **布局性能**
   - 跨端渲染通过 Kuikly Core 原子组件实现，性能接近原生
   - 复杂嵌套布局建议进行性能测试

### 使用示例

```kotlin
@Composable
fun ExampleLayout() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("标题")
        
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("左侧")
            Text("右侧")
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("居中内容")
        }
    }
}
```

### 下一步

- 了解 Material3 组件支持情况，请查看：[组件](./components.md)
- 了解列表与滚动，请查看：[列表与滚动](./lists-and-scrolling.md)

