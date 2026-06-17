---
AIGC:
    Label: "1"
    ContentProducer: 001191110102MACQD9K64018705
    ProduceID: 7635613819561148735-data_volume/files/所有对话/主对话/projects/edgeside/design.md
    ReservedCode1: ""
    ContentPropagator: 001191110102MACQD9K64028705
    PropagateID: 1294603847211091#1781316738458
    ReservedCode2: ""
---
# EdgeSide 设计文档

## 1. 项目概述

### 1.1 项目背景
- **背景说明**：Android 系统级的"边缘工具"目前主要被游戏工具箱、录屏浮窗、聊天浮窗等专用场景占据，缺少一个通用、轻量、可高度自定义的"边缘侧拉面板"。
- **问题分析**：
  - 第三方启动器（Nova / Lawnchair 等）的抽屉/侧栏做不到跨 app 调用
  - 系统"最近任务"无法快速启动 app + 查看即时信息（通知/电池/网速）
  - Game Turbo 类工具功能固化（性能监控/加速/屏蔽通知），用户无法自定义
- **解决方案概述**：一个独立的 Android app，悬浮在所有 app 之上，提供"边缘小条 → 滑出/点击 → 弹出快捷面板"，面板内容由用户自配置（app 快捷方式 + 信息卡片）。

### 1.2 项目目标
- **核心目标**：提供一个跨 app 可调用的边缘悬浮快捷入口，体感类似系统级游戏工具箱的形态。
- **量化指标**：
  - 冷启动悬浮小条 ≤ 1s（开机自启后）
  - 滑出面板响应 ≤ 100ms
  - 内存常驻 ≤ 50MB（不含面板展开时）
  - 适配 Android 8.0 ~ 14（minSdk 26, targetSdk 34���

### 1.3 项目范围
- **包含的功能**：
  - 屏幕边缘小条（贴边、可拖动、磁吸）
  - 面板滑出/收起（滑动 + 点击双触发）
  - 面板内 app 快捷方式（手选自添加）
  - 面板内信息卡片（通知/电池/网速/剪贴板/最近文本）
  - 主 app 配置界面（钉选 app、管理卡片、调节小条位置和大小）
- **不包含的功能**（V1）：
  - 性能监控（FPS/CPU 占用）
  - 加速/清理内存
  - 通知屏蔽
  - 录屏/截屏
  - AI 入口（如需要作为后续版本扩展）

### 1.4 用户画像
- **主要用户群体**：技术爱好者 / 效率工具控 / 重度手机用户
- **用户特征**：熟悉 Android 权限授权流程，能接受"为悬浮窗开几个权限"
- **使用场景**：
  - 场景 1：阅读时想快速切到翻译/计算器 app
  - 场景 2：游戏中想看电量/网速/最新通知
  - 场景 3：工作 app 中想快速复制粘贴/查看剪贴板历史

---

## 2. 需求分析

### 2.1 功能需求

#### 2.1.1 核心功能

| 功能编号 | 功能名称 | 功能描述 | 优先级 |
|---------|---------|---------|-------|
| F001 | 边缘小条 | 屏幕左/右边缘贴边的细长小条，宽 6-8dp，高 80-120dp，半透明，显示"≡"或 logo | 高 |
| F002 | 小条拖动 | 沿屏幕边缘上下拖动改变垂直位置，松手后磁吸到拖动结束点 | 高 |
| F003 | 小条磁吸 | 拖动结束后小条自动贴向最近的屏幕边缘 | 高 |
| F004 | 面板滑出 | 在小条上点击或向屏幕内滑动，触发面板滑出动画 | 高 |
| F005 | 面板收起 | 点击面板外部/再次点击小条/向边缘滑回，触发收起动画 | 高 |
| F006 | app 快捷方式 | 面板顶部显示用户钉选的应用图标网格，点击启动对应 app | 高 |
| F007 | 信息卡片区 | 面板下半部显示可配置的信息卡片（通知/电池/网速/剪贴板/最近文本） | 高 |
| F008 | 主 app 配置 | 进入主 app 可配置：钉选 app、卡片开关、小条位置/大小、主题色 | 高 |

#### 2.1.2 辅助功能

| 功能编号 | 功能名称 | 功能描述 | 优先级 |
|---------|---------|---------|-------|
| F101 | 小条位置记忆 | 关闭再开启后保留上次位置和贴边方向 | 中 |
| F102 | 应用列表扫描 | 主 app 内扫描已安装 app，支持搜索/筛选/拖拽排序 | 中 |
| F103 | 通知读取 | 通过 NotificationListenerService 监听系统通知并展示摘要 | 中 |
| F104 | 网速监控 | 实时计算当前上下行网速（基于 TrafficStats） | 中 |
| F105 | 剪贴板监听 | 监听剪贴板变化，记录最近 5 条文本 | 中 |
| F106 | 电池信息 | 实时显示电量百分比和充电状态 | 中 |
| F107 | 自启引导 | 引导用户将 app 加入各厂商"自启动白名单" | 中 |

#### 2.1.3 扩展功能（可选）

| 功能编号 | 功能名称 | 功能描述 | 优先级 |
|---------|---------|---------|-------|
| F201 | 主题切换 | 主色 #1976D2 / 自定义色 | 低 |
| F202 | 小组件 | 面板内支持用户自定义小组件（脚本入口/AI 入口） | 低 |
| F203 | 双边小条 | 屏幕左右两侧都可放小条 | 低 |
| F204 | 手势扩展 | 自定义滑动手势触发不同动作 | 低 |

### 2.2 非功能需求

#### 2.2.1 性能要求
- 悬浮小条启动 ≤ 1s
- 面板滑出动画 ≤ 100ms 响应
- 内存常驻 ≤ 50MB（不含面板展开时）
- 通知监听 Service 持续运行但事件驱动，不做轮询

#### 2.2.2 安全要求
- 仅请求必要权限：SYSTEM_ALERT_WINDOW、POST_NOTIFICATIONS、READ_LOGS（可选）
- 通知读取��在内存中处理，不上传
- 剪贴板内容仅本地存储

#### 2.2.3 可用性要求
- 首次安装：清晰引导授权流程（按顺序：悬浮窗 → 通知 → 自启动）
- 关键操作可一键撤销（关闭悬浮窗服务）
- 错误状态可重试，不卡死

#### 2.2.4 兼容性要求
- **系统版本**：Android 8.0 ~ 14（minSdk 26, targetSdk 34）
- **厂商适配**：MIUI/HyperOS、EMUI/HarmonyOS、ColorOS、OriginOS、OneUI
  - 自启动白名单引导
  - 后台保活策略
  - 电池优化豁免
- **架构**：arm64-v8a, armeabi-v7a（Kotlin/Java 不需要 native lib）
- **屏幕**：手机优先（5"-7"），平板不做专门适配

### 2.3 约束条件

#### 2.3.1 技术约束
- **必须使用**：Kotlin（行业标准）、Jetpack Compose（主 app 内）
- **悬浮窗本身**：传统 View + WindowManager（Compose 在 WindowManager 上有兼容性坑，V1 不用）
- **禁止使用**：跨平台框架（Flutter/React Native）—— 悬浮窗权限适配成本高

#### 2.3.2 业务约束
- 1 人开发，V1 工期 ~3 周
- 个人项目，无商业化要求
- 不上 Google Play，国内分发走酷安/官网

---

## 3. 系统架构设计

### 3.1 技术栈选型

#### 3.1.1 基础
- **语言**：Kotlin 2.0+
- **构建**：Gradle 8.x + Version Catalog
- **JVM**：JDK 17
- **minSdk / targetSdk / compileSdk**：26 / 34 / 34

#### 3.1.2 主 app UI 层
- **UI 框架**：Jetpack Compose（BOM 2024.06+）
- **导航**：Navigation-Compose
- **主题**：Material Design 2（主色 #1976D2），自定义 Theme（避免 Material 3 的圆角/动态色）
- **状态管理**：ViewModel + StateFlow
- **依赖**：androidx.lifecycle, androidx.activity, androidx.navigation

#### 3.1.3 悬浮窗层
- **容器**：Service（EdgeSideService extends Service）
- **窗口管理**：WindowManager.addView() + TYPE_APPLICATION_OVERLAY
- **小条 UI**：传统 View（自定义 EdgeBarView + 滑动手势检测）
- **面板 UI**：传统 View（EdgePanelView，使用 RecyclerView 展示 app 网格和卡片列表）
- **依赖**：androidx.core, androidx.recyclerview

#### 3.1.4 数据层
- **数据库**：Room 2.6+（存储钉选 app、卡片配置、小条位置）
- **配置存储**：DataStore Preferences（开关、主题色等简单 KV）
- **依赖**：androidx.room, androidx.datastore

#### 3.1.5 后台服务
- **小条服务**：ForegroundService + START_STICKY（被系统杀后自启）
- **通知监听**：NotificationListenerService（用户授权后）
- **网速/电池/剪贴板**：基于 Flow 实时计算（不需独立 Service）

#### 3.1.6 工具
- **日志**：Timber
- **崩溃**：暂不集成（V1 不上生产）
- **依赖注入**：Hilt（V2 再考虑，V1 手写 ViewModelFactory 即可）

**选型理由**：
- Kotlin + Compose 是 Android 行业标准
- 悬浮窗用 View 而非 Compose：WindowManager 兼容性更稳，V1 减少踩坑
- Room + DataStore 是 Jetpack 推荐组合

### 3.2 系统架构图

#### 3.2.1 模块架构
```
┌──────────────────────────────────────────────────────┐
│                   Main App (UI)                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │ HomeScreen   │  │ AppsPickScr  │  │ SettingsScr  │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
│         ↓                  ↓                 ↓        │
│  ┌──────────────────────────────────────────────────┐ │
│  │        ViewModel Layer (StateFlow)              │ │
│  └──────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────┘
              ↓                           ↓
┌──────────────────────────┐  ┌─────────────��──────────┐
│   Data Layer             │  │  Overlay Layer         │
│  ┌────────────────────┐  │  │  ┌──────────────────┐  │
│  │ Room (DB)          │  │  │  │ EdgeSideService  │  │
│  │ DataStore (Prefs)  │  │  │  │  └─ WindowManager│  │
│  └────────────────────┘  │  │  │     ├─ EdgeBar   │  │
│                          │  │  │     └─ EdgePanel │  │
└──────────────────────────┘  │  └──────────────────┘  │
                              └────────────────────────┘
                                          ↑
                              ┌───────────┴──────────┐
                              │  System Services     │
                              │  ├─ NotificationListen│
                              │  ├─ BatteryManager    │
                              │  ├─ ClipboardManager  │
                              │  └─ TrafficStats      │
                              └──────────────────────┘
```

#### 3.2.2 启动流程
```
1. 用户安装 → 启动 Main App
2. Main App 引导授权（悬浮窗 → 通知 → 自启动）
3. 授权完成后启动 EdgeSideService（Foreground）
4. Service 中通过 WindowManager.addView() 注入 EdgeBarView
5. EdgeBarView 显示贴边小条
6. 用户点击/滑动小条 → Service 中 addView(EdgePanelView)
7. EdgePanelView 渲染面板内容（从 Data Layer 读取配置 + 实时数据）
8. 用户操作（点击 app 图标等）→ 触发 Intent / 写入 DataStore
```

### 3.3 架构模式
- **架构模式**：MVVM + 单向数据流（StateFlow）
- **理由**：Android 行业标准，与 Jetpack 生态契合，状态流向清晰（Data → ViewModel → UI → Event → ViewModel → Data）

---

## 4. 功能模块设计

### 4.1 模块划分

| 模块编号 | 模块名称 | 职责描述 |
|---------|---------|---------|
| M001 | MainApp | 主 app 的 Compose UI（首页/选 app/设置） |
| M002 | EdgeSideService | ForegroundService，管理 WindowManager 生命周期 |
| M003 | EdgeBarView | 贴边小条 View，处理拖动/磁吸/触发面板 |
| M004 | EdgePanelView | 面板 View，渲染 app 网格和卡片列表 |
| M005 | NotificationListener | NotificationListenerService，监听系统通知 |
| M006 | DataRepository | Room + DataStore 的统一访问层 |
| M007 | PermissionHelper | 封装各厂商的权限申请流程 |
| M008 | DeviceProfile | 厂商识别 + 适配策略路由 |

### 4.2 核心模块详细设计

#### 4.2.1 M002 EdgeSideService
- **模块职责**：管理悬浮小条和面板的 WindowManager 生命周期，处理系统事件
- **核心功能**：
  - startForeground() 显示常驻通知（"EdgeSide 正在运行"）
  - 启动时 addView(EdgeBarView)
  - 接收 EdgeBarView 的回调：addView(EdgePanelView) / removeView(EdgePanelView)
  - onTaskRemoved() 触发 START_STICKY 自启
  - 监听屏幕开关（BroadcastReceiver）控制小条显示
- **关键代码骨架**：
  ```kotlin
  class EdgeSideService : Service() {
      private lateinit var windowManager: WindowManager
      private var edgeBar: EdgeBarView? = null
      private var edgePanel: EdgePanelView? = null
      
      override fun onCreate() {
          super.onCreate()
          windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
          startForeground(NOTIFICATION_ID, buildNotification())
          showEdgeBar()
      }
      
      private fun showEdgeBar() {
          edgeBar = EdgeBarView(this).apply {
              onPanelToggle = { show -> if (show) showPanel() else hidePanel() }
          }
          windowManager.addView(edgeBar, edgeBarLayoutParams())
      }
      
      private fun showPanel() {
          edgePanel = EdgePanelView(this)
          windowManager.addView(edgePanel, panelLayoutParams())
      }
  }
  ```

#### 4.2.2 M003 EdgeBarView
- **模块职责**：实现可拖动、可磁吸、可触发的边缘小条
- **核心功能**：
  - 自定义 View，宽 6dp × 高 100dp
  - onTouchEvent() 处理 ACTION_DOWN/MOVE/UP：
    - MOVE 时沿 Y 轴移动小条 LayoutParams.y
    - UP 时根据 X 坐标判断贴左/贴右
  - 触发面板：onClick 或水平滑出距离 > 20dp
- **关键属性**：
  - `position: Int`（垂直位置 px，持久化到 DataStore）
  - `edge: Edge.LEFT / Edge.RIGHT`（贴边方向）
  - `onPanelToggle: (Boolean) -> Unit`

#### 4.2.3 M004 EdgePanelView
- **模块职责**：渲染面板内容，处理面板内交互
- **核心功能**：
  - 顶部 4 列 app 网格（RecyclerView + GridLayoutManager）
  - 中部信息卡片（可滚动）
  - 底部空白可点击收起
- **面板布局**（参考形态，不复制游戏工具箱功能）：
  ```
  ┌─────────────────────────┐
  │  ──  app  app  app  app │
  │  ──  app  app  app  app │
  │ ─────────────────────── │
  │  通知：xxx 收到 1 条新通知│
  │  电量：78%               │
  │  网速：↑ 12 KB/s ↓ 1.2MB/s│
  │  剪贴板：xxx             │
  └─────────────────────────┘
  ```
- **数据源**：DataRepository.observeConfig() + Flow 组合（电池/网速/剪贴板）

#### 4.2.4 M005 NotificationListener
- **模块职责**：监听系统通知并生成摘要
- **核心功能**：
  - 继承 NotificationListenerService
  - onNotificationPosted() 时解析通知，存入内存缓存（最近 20 条）
  - 提供 StateFlow<NotificationSummary> 供面板订阅
- **权限要求**：BIND_NOTIFICATION_LISTENER_SERVICE（用户需在系统设置中手动授权）

---

## 5. 数据库设计

### 5.1 数据库选型
- **数据库类型**：SQLite（Room 抽象）
- **版本信息**：Room 2.6+
- **选型理由**：本地存储，关系型够用；Room 编译期校验 SQL，避免运行时崩溃

### 5.2 数据表设计

#### 5.2.1 数据表列表
| 表名 | 说明 | 字段数 |
|------|------|--------|
| pinned_apps | 钉选 app 列表 | 4 |
| panel_config | 面板配置 | 6 |
| bar_config | 小条配置 | 4 |
| notification_cache | 通知缓存（可选，纯内存也行） | 4 |

#### 5.2.2 详细表结构

**表名：pinned_apps**
| 字段名 | 类型 | 非空 | 主键 | 说明 |
|--------|------|------|------|------|
| id | INTEGER | 是 | 是 | 自增 |
| package_name | TEXT | 是 | 否 | app 包名 |
| sort_order | INTEGER | 是 | 否 | 显示顺序 |
| added_at | INTEGER | 是 | 否 | 钉选时间戳 |

**表名：panel_config**
| 字段名 | 类型 | 非空 | 主键 | 说明 |
|--------|------|------|------|------|
| id | INTEGER | 是 | 是 | 固定为 1（单例） |
| show_notifications | INTEGER | 是 | 否 | 显示通知卡片 0/1 |
| show_battery | INTEGER | 是 | 否 | 显示电量卡片 |
| show_network | INTEGER | 是 | 否 | 显示网速卡片 |
| show_clipboard | INTEGER | 是 | 否 | 显示剪贴板卡片 |
| panel_position | TEXT | 是 | 否 | LEFT/RIGHT |

**表名：bar_config**
| 字段名 | 类型 | 非空 | 主键 | 说明 |
|--------|------|------|------|------|
| id | INTEGER | 是 | 是 | 固定为 1 |
| vertical_pos | INTEGER | 是 | 否 | 距顶部 px |
| edge | TEXT | 是 | 否 | LEFT/RIGHT |
| height_dp | INTEGER | 是 | 否 | 小条高度 dp |

### 5.3 数据关系
- 无多表关联需求（各表独立）

### 5.4 数据安全
- 通知内容不持久化（仅内存）
- 剪贴板内容不持久化
- 备份策略：跟随 Android Auto Backup

---

## 6. 接口设计

### 6.1 接口规范
- 本 app 是本地工具，不对外暴露网络 API
- 内部接口：Service ↔ View 通过回调（Lambda）；Repository ↔ ViewModel 通过 Flow

### 6.2 内部回调接口

| 名称 | 触发方 | 接收方 | 说明 |
|------|--------|--------|------|
| onPanelToggle(Boolean) | EdgeBarView | EdgeSideService | true=显示面板，false=隐藏 |
| onAppLaunch(packageName) | EdgePanelView | EdgeSideService | 启动指定 app |
| onConfigChanged() | DataRepository | EdgeSideService | 配置变化通知（可选） |

---

## 7. 安全设计

### 7.1 权限申请
- **首次安装授权顺序**：
  1. `SYSTEM_ALERT_WINDOW`（悬浮窗）
  2. `POST_NOTIFICATIONS`（前台服务通知，Android 13+）
  3. `BIND_NOTIFICATION_LISTENER_SERVICE`（通知监听）
  4. 各厂商"自启动"白名单引导（不是权限申请，是引导用户去系统设置勾选）

### 7.2 数据安全
- 通知/剪贴板内容仅在内存处理，不上传
- 不集成任何分析 SDK
- 不收集任何用户数据

### 7.3 防护措施
- 防止 Service 被杀：START_STICKY + Foreground + 厂商白名单引导
- 防止误触：面板显示时拦截其他触摸事件（FLAG_NOT_TOUCH_MODAL）

---

## 8. 部署方案

### 8.1 部署环境
- **目标设备**：Android 8.0 ~ 14 的真机
- **测试机型清单**：
  - Xiaomi 13 / 14（HyperOS）
  - Huawei Mate 60（HarmonyOS 4）
  - OPPO Find X7（ColorOS 14）
  - vivo X100（OriginOS 4）
  - 三星 S24（OneUI 6）
  - Google Pixel 8（原生）

### 8.2 打包
- **构建类型**：
  - debug：直接 install 到测试机
  - release：APK + AAB（AAB 上架 Google Play 用，V1 暂时不用）
- **签名**：使用个人 debug keystore（V1 阶段）

### 8.3 分发
- 国内：酷安 / 官网 APK
- 国外：暂不分发

### 8.4 升级策略
- 后续版本通过配置 DataStore 的 schema version 处理迁移
- 数据库迁移使用 Room Migration

---

## 9. 开发计划

### 9.1 开发阶段

| 阶段 | 任务 | 工期 | 状态 |
|------|------|------|------|
| 阶段 0 | 需求确认 + 文档（本文档） | 0.5 天 | ✅ 进行中 |
| 阶段 1 | 项目脚手架 + Gradle 配置 | 0.5 天 | 待开始 |
| 阶段 2 | Main App 框架（Compose + Navigation + Theme） | 1 天 | 待开始 |
| 阶段 3 | EdgeSideService + EdgeBarView（拖动/磁吸） | 2 天 | 待开始 |
| 阶段 4 | EdgePanelView + 数据流接入 | 2 天 | 待开始 |
| 阶段 5 | app 钉选 + 启动其他 app | 1 天 | 待开始 |
| 阶段 6 | 信息卡片（电池/网速/剪贴板/通知） | 2 天 | 待开始 |
| 阶段 7 | 厂商适配（MIUI/HyperOS 等） | 2 天 | 待开始 |
| 阶段 8 | 内部测试 + 修 bug | 2 天 | 待开始 |
| **合计** | | **~13 天（3 周）** | |

### 9.2 里程碑
- [ ] M1: EdgeBarView 可拖动贴边、点击触发面板
- [ ] M2: 面板内 app 钉选可用，能跳转
- [ ] M3: 4 个信息卡片全部接入
- [ ] M4: 主要厂商机型自启保活通过
- [ ] M5: 完整一轮自测通过

---

## 10. 风险与应对

| 风险项 | 影响 | 概率 | 应对措施 |
|--------|------|------|---------|
| 厂商 ROM 杀 Service 导致悬浮窗消失 | 高 | 高 | 引导白名单 + START_STICKY + 检测被杀后自启 |
| NotificationListenerService 授权被部分 ROM 阉割 | 中 | 中 | 在主 app 中明示状态，不强求 |
| 悬浮窗被其他 app 拦截（无障碍/全屏模式） | 低 | 中 | 监听屏幕旋转/全屏状态，提示用户 |
| 通知内容解析兼容性（不同 app 通知格式差异大） | 中 | 高 | 只展示 app 名 + 标题，简化处理 |
| WindowManager.addView 在 Compose-only 项目上的兼容坑 | 中 | 中 | 悬浮窗强制用传统 View，绕开 |
| 剪贴板读取 Android 10+ 限制（前台应用才能读） | 中 | 高 | 仅在面板展开时读取，不做后台监听 |

### 厂商适配具体策略
| 厂商 | 关键适配点 |
|------|-----------|
| 小米/HyperOS | 自启动 + 电池优化 + 后台弹出界面 |
| 华为/HarmonyOS | 应用启动管理 + 电池优化 |
| OPPO/ColorOS | 自启动 + 耗电保护 |
| vivo/OriginOS | 后台高耗电 + 自启动 |
| 三星/OneUI | 电池优化（最简单） |
| Google 原生 | 几乎无适配 |

---

## 附录 A. 术语表

| 术语 | 说明 |
|------|------|
| Edge Bar | 屏幕边缘的小条 View |
| Edge Panel | 滑出的快捷面板 |
| TYPE_APPLICATION_OVERLAY | Android 8.0+ 推��的悬浮窗类型 |
| Foreground Service | 前台服务，需显示常驻通知 |
| NotificationListenerService | 系统通知监听服务 |

## 附录 B. 关键依赖（待定）

```toml
[versions]
agp = "8.5.0"
kotlin = "2.0.0"
compose-bom = "2024.06.00"
room = "2.6.1"
lifecycle = "2.8.0"
```

## 附录 C. 变更记录

| 版本 | 日期 | 修改内容 |
|------|------|---------|
| v0.1 | 2026-06-12 | 初稿 |

---

> 本内容由 Coze AI 生成，请遵循相关法律法规及《人工智能生成合成内容标识办法》使用与传播。
