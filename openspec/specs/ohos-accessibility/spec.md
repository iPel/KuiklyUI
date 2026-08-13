# ohos-accessibility

## Purpose

规范 HarmonyOS 端 kuikly 无障碍能力（`accessibility` / `accessibilityRole` / `accessibilityInfo` 属性，`accessibilityAnnounce` / `accessibilityFocus` 方法）的实现契约与跨端一致性要求，覆盖 CAPI 内置组件与 ArkTS 转发组件两条渲染路径。行为对齐 Android（`contentDescription` / `AccessibilityNodeInfo`）与 iOS（`accessibilityLabel` / `accessibilityHint` / `UIAccessibilityTraits`）。

## Requirements

### Requirement: Accessibility 文本朗读 (`accessibility` prop)

HarmonyOS 端的 CAPI 内置组件与 ArkTS 转发组件，SHALL 支持 kotlin `Attr.accessibility(text)` 设置的字符串作为该节点的无障碍朗读内容，行为与 Android `contentDescription`、iOS `accessibilityLabel` 语义对齐。

#### Scenario: CAPI 内置组件的 accessibility 文本朗读（HarmonyOS）

- **GIVEN** 一个 CAPI 内置组件（如 `View`、`Image`）在 kotlin 侧设置了 `attr { accessibility("头像") }`
- **WHEN** 系统开启屏幕朗读，用户聚焦到该组件
- **THEN** 屏幕朗读器 SHALL 播报 "头像"，而不是组件默认内容

#### Scenario: ArkTS 转发组件的 accessibility 文本朗读（HarmonyOS）

- **GIVEN** 一个 ArkTS 转发组件（业务方通过 `viewName()` 注册）在 kotlin 侧设置了 `attr { accessibility("提交按钮") }`，且业务方 `@Component build()` 里最外层容器已按约定应用 `.accessibilityText(this.renderView.cssAccessibilityText ?? "")` 及配套修饰器
- **WHEN** 用户聚焦到该组件
- **THEN** 屏幕朗读器 SHALL 播报 "提交按钮"

#### Scenario: 属性重置（HarmonyOS）

- **WHEN** kotlin 侧将 `accessibility` 属性设为空串或被 view 复用系统重置
- **THEN** CAPI 组件 SHALL 通过 `resetAttribute(NODE_ACCESSIBILITY_TEXT)` 恢复默认；ArkTS 组件 SHALL 将 `cssAccessibilityText` 设为空，业务方修饰器读到空串时不再覆盖默认朗读


### Requirement: Accessibility 角色声明 (`accessibilityRole` prop)

HarmonyOS 端 SHALL 将 kotlin `AccessibilityRole` 枚举映射为鸿蒙无障碍节点角色（或无障碍模式），映射规则如下：

| kotlin role | HarmonyOS 实现 |
|---|---|
| `BUTTON` | `NODE_ACCESSIBILITY_ROLE` = `ARKUI_NODE_BUTTON` |
| `TEXT` | `NODE_ACCESSIBILITY_ROLE` = `ARKUI_NODE_TEXT` |
| `IMAGE` | `NODE_ACCESSIBILITY_ROLE` = `ARKUI_NODE_IMAGE` |
| `CHECKBOX` | `NODE_ACCESSIBILITY_ROLE` = `ARKUI_NODE_CHECKBOX` |
| `SEARCH` | `NODE_ACCESSIBILITY_ROLE` = `ARKUI_NODE_TEXT_INPUT`（降级） |
| `NONE` | `NODE_ACCESSIBILITY_MODE` = `ARKUI_ACCESSIBILITY_MODE_DISABLED`（不设 role） |

对于 ArkTS 转发组件，`KuiklyRenderBaseView` SHALL 提供 `resolveArkUIAccessibilityRole()` 工具函数，把 `cssAccessibilityRole` 字符串翻译为 ArkUI `AccessibilityRoleType` 枚举（`BUTTON / CHECKBOX / IMAGE / SEARCH / TEXT / ROLE_NONE`），业务方在 `@Component build()` 里通过 `.accessibilityRole(this.renderView.resolveArkUIAccessibilityRole())` 一行应用。

#### Scenario: 常规角色映射（HarmonyOS，CAPI 内置组件）

- **GIVEN** 一个 CAPI 内置组件设置 `attr { accessibilityRole(AccessibilityRole.BUTTON) }`
- **WHEN** 用户聚焦到该组件
- **THEN** 屏幕朗读器 SHALL 播报 "按钮" 作为角色提示

#### Scenario: SEARCH 降级映射（HarmonyOS）

- **GIVEN** 一个组件设置 `attr { accessibilityRole(AccessibilityRole.SEARCH) }`
- **WHEN** 用户聚焦到该组件
- **THEN** 该组件 SHALL 被识别为文本输入类角色（`ARKUI_NODE_TEXT_INPUT`），与 Android 端 `EditText` 语义对齐
- **AND** 文档 SHALL 明确说明鸿蒙将 SEARCH 视为单行文本输入

#### Scenario: NONE 剔除无障碍树（HarmonyOS）

- **GIVEN** 一个组件设置 `attr { accessibilityRole(AccessibilityRole.NONE) }`
- **WHEN** 用户在屏幕上滑动焦点
- **THEN** 该 CAPI 组件 SHALL 通过 `NODE_ACCESSIBILITY_MODE = ARKUI_ACCESSIBILITY_MODE_DISABLED` 被跳过；ArkTS 转发组件 SHALL 通过 `.accessibilityLevel('no')` 被跳过
- **AND** 该行为仅剔除本节点，NOT 递归到子节点；子节点仍会独立参与朗读焦点判定（三端一致，语义对齐 Android `IMPORTANT_FOR_ACCESSIBILITY_NO`、iOS `isAccessibilityElement = false`、HarmonyOS `ARKUI_ACCESSIBILITY_MODE_DISABLED`）

#### Scenario: NONE ↔ 常规角色切换（HarmonyOS）

- **GIVEN** 一个 CAPI 组件先设置 `accessibilityRole(NONE)`，后又切换为 `accessibilityRole(BUTTON)`
- **WHEN** 属性变更下发
- **THEN** cpp 侧 SHALL 先 `resetAttribute(NODE_ACCESSIBILITY_MODE)` 清除 DISABLED 残留，再 `setAttribute(NODE_ACCESSIBILITY_ROLE)`，保证节点重新可聚焦并播报新角色

#### Scenario: ArkTS 转发组件角色映射（HarmonyOS）

- **GIVEN** 一个 ArkTS 转发组件设置 `attr { accessibilityRole(AccessibilityRole.BUTTON) }`，且业务方 `@Component build()` 里已应用 `.accessibilityRole(this.renderView.resolveArkUIAccessibilityRole())`
- **WHEN** 用户聚焦到该组件
- **THEN** 屏幕朗读器 SHALL 播报 "按钮" 作为角色提示


### Requirement: Accessibility 交互动作声明 (`accessibilityInfo` prop)

HarmonyOS 端 SHALL 将 kotlin `Attr.accessibilityInfo(clickable, longClickable)` 序列化的两位标志串（如 `"1 0"`）映射为鸿蒙无障碍动作位掩码 `ARKUI_ACCESSIBILITY_ACTION_CLICK | ARKUI_ACCESSIBILITY_ACTION_LONG_CLICK`，同时按当前系统 locale 从 `core-render-ohos` 的 `string.json` 资源读取提示文案（`kuikly_a11y_hint_clickable` / `kuikly_a11y_hint_long_clickable`）拼接后写入 `NODE_ACCESSIBILITY_DESCRIPTION`，供屏幕朗读器聚焦时播报（行为对齐 iOS `accessibilityHint`）。

#### Scenario: 可点击提示（HarmonyOS，CAPI 组件）

- **GIVEN** 一个组件设置 `attr { accessibilityInfo(clickable = true, longClickable = false) }`，系统语言为中文
- **WHEN** 用户聚焦到该组件
- **THEN** 屏幕朗读器 SHALL 在 accessibilityText 之后播报 "双击激活"
- **AND** CAPI 组件通过 `NODE_ACCESSIBILITY_ACTIONS` = `ARKUI_ACCESSIBILITY_ACTION_CLICK` 与 `NODE_ACCESSIBILITY_DESCRIPTION` = 本地化提示字符串同时生效

#### Scenario: 可点击且可长按（HarmonyOS）

- **GIVEN** 一个组件设置 `attr { accessibilityInfo(clickable = true, longClickable = true) }`
- **WHEN** 用户聚焦到该组件
- **THEN** CAPI 组件 SHALL 通过 `NODE_ACCESSIBILITY_ACTIONS` = `ACTION_CLICK | ACTION_LONG_CLICK` 生效，`NODE_ACCESSIBILITY_DESCRIPTION` 拼接为 `"双击激活，双击并长按"`（中文）/ `"Double-tap to activate, Double-tap and hold to long press"`（英文）
- **AND** ArkTS 转发组件 SHALL 通过业务方 `@Component` 应用等价 ArkTS 修饰器生效

#### Scenario: 全部为 false 时重置（HarmonyOS）

- **GIVEN** 一个组件设置 `attr { accessibilityInfo(clickable = false, longClickable = false) }`
- **WHEN** 属性下发
- **THEN** cpp 侧 SHALL 同时 `resetAttribute(NODE_ACCESSIBILITY_ACTIONS)` 与 `resetAttribute(NODE_ACCESSIBILITY_DESCRIPTION)`，不留残留提示

#### Scenario: 提示文案跟随系统 locale（HarmonyOS）

- **GIVEN** `core-render-ohos/src/main/resources/{base,zh_CN,en_US}/element/string.json` 均定义 `kuikly_a11y_hint_clickable` 与 `kuikly_a11y_hint_long_clickable`
- **WHEN** cpp 侧通过 `OH_ResourceManager_GetStringByName` 读取（`res_mgr` 从 `IKRRenderView::GetNativeResourceManager()` 拿）
- **THEN** 系统 SHALL 按当前 locale 命中对应目录的字符串；未命中 locale 时 SHALL 回落 `base/`（英文）
- **AND** 读取结果 MAY 进程级缓存以避免每次 setProp 都做资源查找


### Requirement: 主动播报文本 (`accessibilityAnnounce` method)

HarmonyOS 端 SHALL 支持通过 kotlin `DeclarativeBaseView.accessibilityAnnounce(msg)` 触发屏幕朗读器主动播报指定文本。行为与 Android `announceForAccessibility` 及 iOS `UIAccessibilityAnnouncementNotification` 对齐。

实现路径：cpp `IKRRenderViewExport::CallMethod` 拦截 → 通过 `KRArkTSManager::CallArkTSMethod(CallViewMethod, viewTag, ...)` 桥到 ArkTS。ArkTS 侧 `KRNativeInstance.callViewMethod` SHALL 提供全局兜底：当 `viewTag` 已注册（ArkTS 转发组件）时委托给 view 的 `toCall(...)`；未注册（CAPI 内置组件）时直接调 `KuiklyRenderBaseView.postAccessibilityAnnounce(bundleName, message)` 静态方法完成播报。最终一律调用 `@ohos.accessibility.sendAccessibilityEvent`（`type = 'announceForAccessibility'`，`textAnnouncedForAccessibility = msg`，`bundleName` 从 `UIContext.getHostContext().applicationInfo.name` 动态取）。

#### Scenario: CAPI 组件主动播报（HarmonyOS）

- **GIVEN** 系统开启了屏幕朗读，某 CAPI 内置组件（如 `View`）
- **WHEN** kotlin 侧调用 `view.accessibilityAnnounce("提交成功")`
- **THEN** 桥接 SHALL 走 KRNativeInstance 兜底路径直接触发系统事件，即使 ArkTS 侧 `viewRegistry` 无该 tag 也 NOT 静默丢弃
- **AND** 屏幕朗读器 SHALL 播报 "提交成功"，无论该 view 是否处于焦点

#### Scenario: ArkTS 转发组件主动播报（HarmonyOS）

- **GIVEN** 系统开启了屏幕朗读，某 ArkTS 转发组件
- **WHEN** kotlin 侧调用 `view.accessibilityAnnounce("提交成功")`
- **THEN** 桥接 SHALL 走 view 实例的 `KuiklyRenderBaseView.call` 分支，行为与 CAPI 组件等价

#### Scenario: bundleName 兜底（HarmonyOS）

- **GIVEN** ArkTS 侧 `UIContext.getHostContext().applicationInfo.name` 返回空
- **WHEN** kotlin 侧调用 `view.accessibilityAnnounce("测试")`
- **THEN** 系统 SHALL 记录 `console.error` 并静默 no-op，NOT 抛出未捕获异常


### Requirement: 无障碍焦点跳转 (`accessibilityFocus` method)

HarmonyOS 端 SHALL 支持通过 kotlin `DeclarativeBaseView.accessibilityFocus()` 请求将屏幕朗读器的焦点跳转到调用该方法的 view 上。行为与 Android `TYPE_VIEW_ACCESSIBILITY_FOCUSED` 事件、iOS `UIAccessibilityScreenChangedNotification` 对齐。

实现路径：cpp `IKRRenderViewExport::CallMethod` 拦截 `accessibilityFocus` 时，SHALL 先将 kuikly 侧 `nodeId` 通过 `setAttribute(NODE_ID)` 写入 CAPI 节点，然后把 `nodeId` 作为 `params` 覆盖后调 `CallArkTSMethod(CallViewMethod, ...)`。ArkTS 侧 `KRNativeInstance.callViewMethod` 未注册 tag 时 SHALL 直接调 `KuiklyRenderBaseView.postAccessibilityFocus(bundleName, customId)`（其中 `customId` 就是 `params` 传入的 `nodeId`）。最终调用 `@ohos.accessibility.sendAccessibilityEvent`（`type = 'requestFocusForAccessibility'`，`customId = nodeId`）。

业务方 ArkTS 组件的最外层容器 MUST 应用 `.id(this.renderView.getNodeId())`，使 `customId` 能定位到目标节点。

#### Scenario: CAPI 组件焦点跳转（HarmonyOS）

- **GIVEN** 屏幕朗读焦点当前在页面某个位置，一个 CAPI 内置组件 `blueView`
- **WHEN** kotlin 侧调用 `blueView.accessibilityFocus()`
- **THEN** cpp 侧 SHALL 把 nodeId 写入 CAPI 节点的 `NODE_ID`，并把 nodeId 作为 params 桥到 ArkTS
- **AND** ArkTS 侧走全局兜底调用 `sendAccessibilityEvent`，焦点框 SHALL 跳转到 `blueView`

#### Scenario: ArkTS 组件焦点跳转（HarmonyOS）

- **GIVEN** 屏幕朗读焦点当前在页面某个位置，一个 ArkTS 转发组件 `yellowView` 的最外层容器已应用 `.id(this.renderView.getNodeId())`
- **WHEN** kotlin 侧调用 `yellowView.accessibilityFocus()`
- **THEN** 屏幕朗读器的焦点框 SHALL 跳转到 `yellowView`

#### Scenario: 焦点跳转失败（HarmonyOS）

- **GIVEN** 业务方遗漏了 `.id(this.renderView.getNodeId())` 修饰器
- **WHEN** kotlin 侧调用 `view.accessibilityFocus()`
- **THEN** `accessibility.sendAccessibilityEvent` SHALL 触发但焦点不会跳转（`customId` 匹配不到目标）
- **AND** 系统 NOT 抛出未捕获异常，仅日志记录


### Requirement: 双渲染路径覆盖

HarmonyOS 端的无障碍属性/方法实现 SHALL 同时覆盖 CAPI 内置组件（`KRView`、`KRImageView` 等，走 `KRBasePropsHandler`）与 ArkTS 转发组件（`KRForwardArkTSView(V2)`，走 `KRArkTSViewBasePropsHandler` no-op 后桥到 ArkTS），两条渲染路径的行为等价。

#### Scenario: CAPI 组件属性通过 cpp 生效（HarmonyOS）

- **GIVEN** 一个 CAPI 内置组件
- **WHEN** kotlin 设置 `accessibilityRole` / `accessibilityInfo`
- **THEN** cpp `KRBasePropsHandler::SetPropWithoutAnimation` SHALL 调用 CAPI `setAttribute` 生效

#### Scenario: ArkTS 组件属性通过 ets 生效（HarmonyOS）

- **GIVEN** 一个 ArkTS 转发组件
- **WHEN** kotlin 设置 `accessibilityRole` / `accessibilityInfo`
- **THEN** 属性 SHALL 通过 `SetViewProp` 桥到 ArkTS `KuiklyRenderBaseView.setProp`，写入 `cssAccessibilityRole` / `cssAccessibilityInfo` 字段
- **AND** 业务方 `@Component build()` 从 `this.renderView` 读取对应字段并应用 ArkTS 修饰器
- **AND** cpp 侧不得对该组件的 `ark_node_` 直接调 `setAttribute(NODE_ACCESSIBILITY_*)`（会返回 `ARKUI_ERROR_CODE_ARKTS_NODE_NOT_SUPPORTED = 106103`）

#### Scenario: announce/focus 两类组件行为一致（HarmonyOS）

- **GIVEN** 一个 CAPI 内置组件 A 和一个 ArkTS 转发组件 B
- **WHEN** 分别调用 `A.accessibilityAnnounce("hi")` 与 `B.accessibilityAnnounce("hi")`
- **THEN** 两者 SHALL 通过同一份 ArkTS 静态实现（`KuiklyRenderBaseView.postAccessibilityAnnounce`）最终调用 `@ohos.accessibility.sendAccessibilityEvent`，行为等价


### Requirement: 文档补齐

`docs/API/components/basic-attr-event.md` SHALL 补充 `accessibility`、`accessibilityRole`、`accessibilityInfo`、`accessibilityAnnounce`、`accessibilityFocus` 五项 API 的完整说明，含跨端支持矩阵（Android / iOS / HarmonyOS）与鸿蒙特有的 role 映射规则；`docs/DevGuide/` 下 SHALL 新增一份"鸿蒙自定义 ArkTS 组件无障碍接入指南"。

#### Scenario: API 文档包含跨端差异说明

- **WHEN** 开发者查阅 `basic-attr-event.md` 中的 `accessibilityRole` 章节
- **THEN** 文档 SHALL 展示 role 枚举在 Android / iOS / HarmonyOS 三端的映射对照表
- **AND** 明确说明鸿蒙 SEARCH → `TEXT_INPUT`、NONE → `MODE_DISABLED` 的降级规则
- **AND** 说明 `NONE` 只剔除本节点、NOT 递归到子节点（三端一致）

#### Scenario: 业务方接入指南可复制粘贴

- **WHEN** 业务方阅读 `docs/DevGuide/ohos-custom-accessibility.md`
- **THEN** 文档 SHALL 提供一个完整的 `@Component build()` 代码片段作为模板，展示如何应用 `.id()` / `.accessibilityText()` / `.accessibilityRole()` / `.accessibilityLevel()` / `.accessibilityGroup()` 五件套
- **AND** 说明为什么子节点需要 `.accessibilityLevel('no')`
- **AND** 说明 `resolveArkUIAccessibilityRole()` 工具函数的使用方式
