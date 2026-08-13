# 鸿蒙自定义 ArkTS 组件无障碍接入指南

## 背景

Kuikly 在鸿蒙上有两条渲染路径：

- **CAPI 内置组件**（`KRView` / `KRImageView` 等）：由 cpp 层通过 CAPI `NODE_ACCESSIBILITY_ROLE / TEXT / ACTIONS` 直接设置无障碍属性，**业务方零改动**。
- **ArkTS 转发组件**（业务方通过 `viewName()` 注册的自定义组件）：由于鸿蒙 SDK 明确规定"CAPI 不能修改 ArkTS 侧创建的节点"（`setAttribute` 会返回 `ARKUI_ERROR_CODE_ARKTS_NODE_NOT_SUPPORTED = 106103`），无障碍属性必须**通过 ArkTS 侧的修饰器**在业务方自己的 `@Component` 里应用。

本文档面向 ArkTS 自定义组件的业务方开发者，讲解如何最小改动接入无障碍。

## 最小接入模板

假设你有一个自定义组件 `KRHelloView`，先让它的 `KuiklyRenderBaseView` 子类接受 kuikly 的 `accessibility*` 属性：

```typescript
import { KRAny, KuiklyRenderBaseView, KuiklyRenderCallback } from '@kuikly-open/render';

@Observed
export class KRHelloView extends KuiklyRenderBaseView {
  static readonly VIEW_NAME = 'KRHelloView';

  // 你自己的业务字段
  cssMessage: string | null = null;

  setProp(propKey: string, propValue: KRAny | KuiklyRenderCallback): boolean {
    switch (propKey) {
      case 'message':
        this.cssMessage = propValue as string;
        return true;
      default:
        // 关键：让基类处理 backgroundColor / backgroundImage / accessibility / accessibilityRole / accessibilityInfo
        return super.setProp(propKey, propValue);
    }
  }

  call(method: string, params: KRAny, callback: KuiklyRenderCallback | null): void {
    // 未识别的 method 交给基类兜底（含 accessibilityAnnounce / accessibilityFocus）
    super.call(method, params, callback);
  }

  createArkUIView(): ComponentContent<KuiklyRenderBaseView> { /* ... */ }
}
```

然后，**在 `@Component` 的最外层容器上应用 5 个修饰器**：

```typescript
@Component
export struct KRHelloViewComponent {
  @ObjectLink renderView: KRHelloView;

  build() {
    Stack() {
      Text(this.renderView.cssMessage)
        // 阻止子节点抢占无障碍焦点，让外层 Stack 的 accessibilityText 生效
        .accessibilityLevel('no')
    }
    // === 无障碍接入五件套 ===
    // 1. .id() 提供 customId 供 accessibilityFocus 定位
    .id(this.renderView.getNodeId() || 'KRHelloView')
    // 2. .accessibilityText() 应用 kuikly 的 accessibility 属性
    .accessibilityText(this.renderView.cssAccessibilityText ?? '')
    // 3. .accessibilityRole() 由基类工具函数把 kuikly 字符串 role 翻译为 ArkUI 枚举
    .accessibilityRole(this.renderView.resolveArkUIAccessibilityRole())
    // 4. .accessibilityLevel() 声明可被无障碍服务识别；none role 时降级为 'no'
    .accessibilityLevel(this.renderView.cssAccessibilityRole === 'none' ? 'no' : 'yes')
    // 5. .accessibilityGroup(true) 把 Stack 视作一个整体聚焦单位
    .accessibilityGroup(true)
    // === 以下是你自己的样式 ===
    .backgroundColor(this.renderView.cssBackgroundColor)
    .size({ width: '100%', height: '100%' })
  }
}
```

## 关键点解释

### 为什么子节点必须 `.accessibilityLevel('no')`？

ArkUI 的无障碍焦点会**优先落到有内容的子节点**（如 `Text`、`Button`），而不是外层容器。如果不显式让子节点退出无障碍树，屏幕朗读器会念子节点的默认内容（如 `Text` 的文本、`Button` 的"按钮 单指双击即可执行"），**外层 `.accessibilityText(...)` 反而失效**。

阻止方式二选一：

- **方式 A（推荐，兼容内部交互）**：给每个非核心子节点加 `.accessibilityLevel('no')`。精确、可控，不影响子节点的正常触摸/点击。
- **方式 B（整体聚焦）**：外层容器加 `.accessibilityGroup(true)`。把整个子树视作一个焦点单元，一次描述完整组件语义；但**开启读屏后，内部子控件的原生手势（如 `Button.onClick`）也会被拦截**，用户只能通过"单指双击整个组"激活外层默认动作。若组件内部有需要独立点击的子控件，请勿使用此方式。

模板里两者都用是为了作为"零内部交互"参考的兜底示例。**在生产代码里通常二选一即可**：

| 组件形态 | 建议 |
|---|---|
| 卡片仅作展示（无内部可点子控件） | 方式 B 更省事：外层 `.accessibilityGroup(true)`，子节点可不动 |
| 卡片内含独立可点子控件（如 Button、可点子区域） | **只用方式 A**：给子控件外的其他节点各自 `.accessibilityLevel('no')`；**不要**加 `.accessibilityGroup(true)`，否则子控件在读屏下无法点击 |
| 混合场景 | 优先方式 A；确需整体聚焦时接受子控件不可独立点击的代价 |

### 为什么必须 `.id(getNodeId())`？

`accessibilityFocus` 方法通过 ArkTS `accessibility.sendAccessibilityEvent({ type: 'requestFocusForAccessibility', customId: nodeId })` 触发。系统按 `customId` 反查目标节点，而 `customId` 匹配的正是组件通过 `.id(...)` 设置的值。**遗漏 `.id()` 会导致 `accessibilityFocus` 静默失效**（不抛异常，但焦点不跳转）。

### `accessibilityInfo` 怎么用？

kuikly 侧 `accessibilityInfo(clickable, longClickable)` 会写入 `renderView.cssAccessibilityInfo`，格式是 `"1 0"` / `"1 1"` 这样的位串。业务方需自行拆解并应用 ArkTS 修饰器（如给 `Button` 加 `.onClick(...)` + `.onLongPress(...)`）。若你的自定义组件本身是标准可点击容器，一般不需要额外处理。

### `resolveArkUIAccessibilityRole()` 都映射了哪些 role？

kuikly 的 `AccessibilityRole` 枚举取值有限，基类内置了以下映射；未识别或未设置时回落到 `ROLE_NONE`：

| kuikly `AccessibilityRole` | 字符串值 | ArkUI `AccessibilityRoleType` |
|---|---|---|
| `BUTTON` | `"button"` | `BUTTON` |
| `CHECKBOX` | `"checkbox"` | `CHECKBOX` |
| `IMAGE` | `"image"` | `IMAGE` |
| `SEARCH` | `"search"` | `SEARCH` |
| `TEXT` | `"text"` | `TEXT` |
| `NONE` / 未设置 | `"none"` / `null` | `ROLE_NONE` |

若需要更细粒度的 ArkUI 角色（如 `SEARCH_FIELD` / `TAB_BAR` 等 kuikly 未暴露的类型），可以直接在业务方 `@Component` 里读取 `renderView.cssAccessibilityRole` 自行判断并应用。

### accessibilityAnnounce / accessibilityFocus 需要业务方做什么？

**只要 `KuiklyRenderBaseView.call` 里调了 `super.call(method, params, callback)`，就自动支持**。基类已经通过 `@ohos.accessibility.sendAccessibilityEvent` 实现了两个方法。业务方无需额外代码。

## 常见问题

**Q：屏幕朗读没念 `accessibility` 的文本，而是念了子 `Text` 的内容？**
A：忘了给子节点加 `.accessibilityLevel('no')`。

**Q：`view.accessibilityFocus()` 调了但焦点没跳？**
A：外层容器忘了 `.id(this.renderView.getNodeId())`；或者你手动指定了固定 id 字符串（会覆盖 nodeId）。

**Q：announce 时抛 `code: 401`？**
A：`accessibility.EventInfo` 的 `bundleName` 必填。基类已用 `UIContext.getHostContext().applicationInfo.name` 动态取；若在极早的初始化阶段（`UIContext` 尚未就绪）调用会为空，此时基类会 log 警告并 no-op，不影响后续。

**Q：为什么 CAPI 内置组件（如 kuikly 的 `View`）不需要做任何修改？**
A：CAPI 组件走 cpp 层的 `KRBasePropsHandler` 直接 `setAttribute(NODE_ACCESSIBILITY_*)`，不经过 ArkTS 层。修饰器仅对业务方自定义的 ArkTS 转发组件是必需的。

**Q：读屏开启后，自定义组件里的 `Button.onClick` 点不动？**
A：外层容器加了 `.accessibilityGroup(true)`。这会把整个子树聚合成一个焦点单元，读屏模式下拦截内部子控件的原生手势，用户只能"单指双击整个组"触发外层默认动作。若需要子控件独立可点，请去掉 `.accessibilityGroup(true)`，只给非核心子节点加 `.accessibilityLevel('no')` 抑制读屏焦点。详见上文"为什么子节点必须 `.accessibilityLevel('no')`?"。

## 参考

- Kuikly 基础 accessibility API：[basic-attr-event.md](../API/components/basic-attr-event.md#accessibility方法)
- 完整示例：`ohosApp/entry/src/main/ets/kuikly/components/KRMyDemoCustomView.ets`
- ArkTS 无障碍 API：[@ohos.accessibility (系统能力：SystemCapability.BarrierFree.Accessibility.Core)](https://developer.huawei.com/consumer/cn/doc/harmonyos-references/js-apis-accessibility)
