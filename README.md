# EdgeSide

屏幕边缘悬浮快捷工具。详细设计见 `../design.md`。

## 当前状态（V1 阶段 0/1）

已完成：
- 工程脚手架（Gradle Kotlin DSL + Version Catalog）
- Manifest 权限 / Foreground Service / NotificationListener / BootCompleted Receiver
- 主 app UI（Compose + Material 2 主题）：Home / Permission / AppsPick / Settings
- Data 层（Room + DataStore + 简易 DI 容器）
- 悬浮服务：EdgeSideService（ForegroundService） + EdgeBarView（拖动/磁吸/触发） + EdgePanelView（app 网格 + 信息卡）
- 3 个数据源 monitor：BatteryMonitor / NetworkMonitor / ClipboardMonitor
- 通知监听：EdgeNotificationListener
- 厂商适配：DeviceProfile（MIUI / 华为 / OPPO / vivo / 三星 / Google）
- 资源：strings / colors / themes（浅色+深色）/ adaptive icon / 面板 layout

未完成（V1 后续）：
- 信息卡片"最近文本"（现只展示剪贴板第一条）
- 自启保活实测（依赖 START_STICKY + 厂商白名单引导，V1 文档里已写）
- WorkManager 兜底拉起（替代纯 START_STICKY，更稳）
- 真实设备联调

## 编译

### 导入到 Android Studio

1. Android Studio Ladybug (2024.2.1) 或更新
2. 打开 `android/` 目录
3. 等待 Gradle sync 完成
4. Run app（target：Android 8.0+ 真机）

### 命令行

```bash
cd android
./gradlew assembleDebug    # 生成 app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug     # 安装到连接的设备
```

注意：
- 需要本机有 Android SDK Platform 34
- `local.properties` 里 `sdk.dir=/path/to/Android/sdk`
- 首次 sync 会下载 Compose / Room / Navigation 等依赖

## 测试机型建议

- Xiaomi 13/14（HyperOS）
- Huawei Mate 60（HarmonyOS 4）
- OPPO Find X7（ColorOS 14）
- vivo X100（OriginOS 4）
- 三星 S24（OneUI 6）
- Google Pixel 8（原生）

## 已知小问题（跑起来后可能要手动修）

1. EdgeBarView 的"水平滑动 24dp 触发面板"阈值需要根据手感调
2. EdgePanelView 的 infoAdapter 用 `notifyDataSetChanged`，会有轻微闪烁；V1.1 改 DiffUtil
3. AppsPickScreen 排除系统 app 时保留了"设置/相机/电话"三个白名单，可按需调整（`KEEP_SYSTEM` set）
4. EdgeSideService 的 onTaskRemoved 仅靠 START_STICKY，国产 ROM 不够稳；V1.1 加 WorkManager

## 关键决策

详见 `../design.md` 和 `../notes.md`。
