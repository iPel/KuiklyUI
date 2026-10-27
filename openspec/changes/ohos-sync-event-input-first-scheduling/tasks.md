> 实施说明：代码已在分支 `ohos-sync-event-scheduling` 全部实现。未勾选：3.6、4.9、5.6、5.7、8.1。

## 1. 准备

- [x] 1.1 从 `main` 新建实现分支（实验分支 `sync-event-perf` 中的 `KRSyncEventPerf` 全量统计、测试页、`tools/sync_event_perf/` 不合入，仅作参考与回归工具）
- [x] 1.2 在实现分支保留一份轻量回归页与 bench 脚本（不随正式代码合入），用于每阶段与基线对比。页：`demo/.../debug/SyncEventPerfPages.kt`（`SyncTouchMovePerfPage` / `SyncScrollPerfPage` / `SyncComplexPerfPage`）、`SyncComplexComposePerfPage.kt`。脚本：本目录 `bench.sh`

## 2. renderer：单线程模式死锁修复（D4，可独立合入）

- [x] 2.1 `KRContextSchedulerSingleThreaded::ScheduleTask`：delay ≤ 0 改为 `KRMainThread::RunOnMainThreadForNextLoop`，delay > 0 保持定时器
- [x] 2.2 HarmonyOS 真机验证：`KRSetThreadingMode(1)` 下 demo 首屏正常渲染、无 appfreeze；多线程模式回归无变化
  - 2026-10-22 设备 2LQ0223A26007226。`aa start --pi krThreadingMode 1`：`threadingMode set to single` 后 `created threadingMode=single`，路由页首屏出来，进程存活，hilog 无 APP_FREEZE。不带参数冷启动：`created threadingMode=multi`，同一首屏，无 freeze。符号不进 `Kuikly.h`，demo napi 在 initKuikly 前调用。

## 3. renderer：当前任务做完后释放锁，剩余任务放回队首（D2）

- [x] 3.1 `KRThread::TryRunOnCurThread(window, task)`：返回是否同步执行；`DirectRunOnCurThread` 改为基于它实现，保持原 100ms 降级语义
- [x] 3.2 `KRThread`：外部等待者计数（首次 `try_lock` 失败登记，获得锁或放弃时注销，RAII）
- [x] 3.3 `KRThread::OnAsync`：任务之间看有没有人在等锁。有的话，先把剩余任务按原顺序放回 `m_pending` 队首，再释放锁。等计数归零后回到 loop 重新调度
- [x] 3.4 `KRContextScheduler::TryDirectRunOnMainThread`；`KRRenderCore` 同步事件路径改用该接口，降级时投递语义与原实现一致
- [x] 3.5 debug 断言：主线程拿着 context 任务锁时再次进入 `TryRunOnCurThread`
- [ ] 3.6 HarmonyOS 真机验证：多小任务负载下同步 touchMove 等锁上限降至单个 task；FIFO 顺序校验（序号断言）；canvas / 列表懒加载 / Compose 动画回归

## 4. renderer：连续同步事件的开始和结束，以及帧任务等本帧输入（D0 + D1）

- [x] 4.1 新增 `scheduler/KRInputFirstGate` 会话管理：以"view + 事件类型"为键的会话开始/结束（幂等）、引用计数、按 view 释放全部会话
- [x] 4.2 `KRView`：注册了同步 `touchMove` 时，首个 pointer DOWN 开始会话、最后一个 pointer UP/CANCEL 结束；view 移除/销毁时释放
- [x] 4.3 `KRScrollerView`：注册了同步 `scroll` 时，ArkUI 滚动开始/停止上报会话（覆盖拖动、惯性、带动画 contentOffset）；移除/销毁时释放
- [x] 4.4 键盘高度事件：确认鸿蒙 `keyboardHeightChange` 是否逐帧触发及动画开始/结束标志；注册了同步时上报会话；手势 `state` start/end 会话接口预留（结论：非逐帧，不纳入会话，见 design.md D0；手势会话接口已预留）
- [x] 4.5 `KRRenderCore`：同步连续事件回调前通知"本帧输入开始"，Kotlin 回调与同步 UI 提交完成后通知"本帧输入处理完成"
- [x] 4.6 `KRInputFirstGate` 帧任务状态机：不在会话 / 本帧输入已处理 / 输入空闲 → 立即投递；否则暂存，输入处理完成时投递；下一 vsync 到达仍暂存则合并投递并进入输入空闲；下一次输入开始退出输入空闲；无任何时间阈值
- [x] 4.7 `KRVsyncModule::OnVsync`：一帧的投递交给 `KRInputFirstGate`。有暂存时，新的 vsync 从「丢掉」改为「合并后投递」。真正执行时再检查 running 和 generation
- [x] 4.8 `KRVsyncModule`：暴露最近 vsync 时间戳与实测帧间隔（供第 5 组配额计算）
- [ ] 4.9 HarmonyOS 真机验证：Compose vsync 负载下同步 touchMove 等锁 p90 显著下降；手指停顿时帧节拍不减半、独立动画至多停顿开始时损失一帧；多指与 touch + scroll 并存的会话边界正确；手势中途移除 view 会话被释放；页面销毁时暂存投递被安全丢弃
    - 2026-10-09 部分完成（设备 2LQ0223A26007226，debug 包，SyncEventPerfPages 4ms/帧 vsync 负载，uitest 模拟）：touch / scroll 会话边界正确（含惯性）；等锁 p90 ≈ 0.013ms，响应延迟 p50/p90 ≈ 11.8/13.1ms；长按停顿仅合并 1 帧后进入输入空闲（frameDirect=180），帧节拍不减半；无断言。未测：多指、touch+scroll 并存、手势中途移除 view、销毁时暂存丢弃、Compose 页面

## 5. renderer：连续同步事件期间由主线程执行 context 任务（D3，每轮最多 2ms）

- [x] 5.1 `KRThread`：持有 context 任务锁时取下一个任务的接口，供主线程每一轮调用
- [x] 5.2 `KRThread` 暂停/恢复：`SetParked` 与暂停期间的待执行通知；进入暂停时正在执行的 task 结束后（含等锁期间进入暂停）剩余批次退回队首、不再执行；恢复时唤醒 worker
- [x] 5.3 `KRMainThreadTaskPuller`：连续同步事件从无到有时暂停 context 线程并投递一轮，从有到无时恢复。每一轮最多 2ms，到时间再投下一轮，队列空就停。`KRInputFirstGate` 在开始和结束时通知
- [x] 5.4 主线程正在执行 context 任务时，产生的 UI 任务等锁释放后再执行（`PerformOnMainQueueWithTask`）
- [x] 5.5 不导出可以关掉这段行为的 C 函数。同步 touchMove 固定注册。若 context 任务仍留在 context 线程，同步 touchMove 会等完整个帧任务
- [ ] 5.6 HarmonyOS 真机验证：会话期间 context 线程不执行任务（可通过 worker 唤醒/执行计数确认）、FIFO 顺序不变（序号断言）、会话结束后 worker 接续积压任务；UI 任务同步触发的 scroll 事件不出现 100ms 降级
- [ ] 5.7 复杂页面 release 版 Kotlin 指标：响应延迟 p50/p90、掉帧率、会话期间主线程 Kuikly 耗时；负载至少覆盖"每帧一个大任务"与"拆分小任务"两类。关闭分支已不保留，见 9.2
    - 以下为初版 D3（worker 照常执行、主线程按每帧配额拉取）的复测记录，因设计有误已作废，仅供参考：
      - 2026-10-09 部分完成：开关关闭时行为与第 4 组一致；开启（`--pi krTaskPull 1`）后无崩溃、无重入断言，但"每帧一个大任务"负载下切片全部以 QueueEmptyOrBusy 停止（每会话约 200 次，拉到 0 个任务）——帧任务投递后 worker 立即取走并持锁执行，单次 try_lock 拿不到。需用 loadMode 2（拆分小任务）复测后再评估 D3 价值
      - 2026-10-09 loadMode 2 复测（`--pi krLoadMs 4 --pi krLoadMode 2`，三轮拖动，`krLoadMs`/`krLoadMode` 已确认透传到 pageData.params）：开关关（`krTaskPull 0`）与开（`krTaskPull 1`）的 touchMove 延迟无可测差异（p50 约 11.1–11.5ms，p90 约 11.8–13.3ms）；开启时 `pullSliceBudget` / `pullFrameQuota` / `pullPredictedCost` 均为 0，`pullQueueEmptyOrBusy` 约等于每次输入（每会话约 140，与 loadMode 0 时同形），即每次输入时要么队列已空、要么 worker 持锁，计数器无法区分二者。结论：在当前负载与交互下 D3 未见收益，保持默认关闭；D3 的有效场景（同步事件到达时主线程队列仍有积压且 worker 空闲）未覆盖，需另行构造后再决定是否默认开启
      - 2026-10-09 复杂列表 Compose 页复测（`SyncComplexComposePerfPage`：200 行、50ms 定时器刷新 40 行、手柄竖向拖拽排序；帧由 ComposeContainer vsync 驱动；拖拽 touchMove 为同步，仅在实验期间本地临时打开 `SuperTouchManager.useSyncMove`，已恢复，未提交）：开关关与开的 latency p50 中位数均为 11.7ms，p90 中位数 21.6 / 21.6ms；lockWait p90 中位数 3.25ms（关）对 3.84ms（开），开启时略差；`pullPredictedCost=2144`、`pullQueueEmptyOrBusy=4238`、`pullSliceBudget=1`、`pullFrameQuota=4`。结论：该负载下 D3 无收益，保持默认关闭。鸿蒙 Compose 同步 move 仍按设计保持关闭（`useSyncMove = !isOhOs`）

## 6. renderer：A/B 指标（D6）

- [x] 6.1 按会话聚合：响应延迟、等锁、会话期间主线程 Kuikly 耗时、帧任务投递分类计数（等待输入 / 合并 vsync / 直接投递）、进入输入空闲次数、切片次数与停止原因
- [x] 6.2 经性能/日志 adapter 输出；会话之外零开销路径
- [x] 6.3 校验指标与实验分支 bench 结果口径一致（已按代码核对：响应延迟 = 输入时间戳 → UI 提交完成，等锁与主线程耗时同口径，统计为 p50/p90/max；差异：实验按"空闲 2.5s 一轮"汇总，本实现按会话汇总）

## 7. demo 宿主（ohosApp，可选，不随正式代码合入）

- [x] 7.1 不透传 D3 开关。本地 A/B 用过的 `setMainThreadTaskPullEnabled` / `krTaskPull` 已从 ohosApp 去掉，不随正式代码合入

## 8. 鸿蒙回归

- [ ] 8.1 HarmonyOS：自研 DSL 同步 touchMove / syncScroll、Compose 列表与动画、canvas、输入框、单线程模式，release 版 Kotlin 跑回归 bench（对照 design.md 数据表）

## 9. 文档与后续

- [x] 9.1 运行 doc-archive-review。接入文档 `docs/QuickStart/harmony.md` 不写内部调度。说明写在 `.ai/references/ohos-sync-event-scheduling.md`（索引已登记）
- [x] 9.2 复杂页面的结论：主线程执行 context 任务，和鸿蒙 Compose 的同步 touchMove，要一起生效。没有单独关掉主线程执行的方式。只改同步 touchMove 时，滚动等锁 p90 10.4ms、延迟 p90 21.4ms；一起生效时 0.008ms / 11.1ms，手势期间上屏仍为 120Hz
- [x] 9.3 立项 Android（`postAtFrontOfQueue`）与 iOS（先处理输入再投递帧任务）后续 change：`openspec/changes/android-sync-event-post-at-front/`、`openspec/changes/ios-sync-event-input-first/`。两者都只写了 proposal，design / specs / tasks 未开始
