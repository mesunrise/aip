# Phone自动化项目

> 基于云端控制+Android App的抖音自动化系统

## 📋 项目概述

Phone自动化是一个采用云端控制+Android App架构的抖音自动化系统，用于自动化采集评论、发送私信等任务。

### 核心特性

- 🌐 **云端控制**：服务器端统一调度和管理任务
- 📱 **Android App**：手机端执行自动化操作
- 🔄 **实时通信**：WebSocket双向通信
- 🤖 **无障碍服务**：AccessibilityService实现UI自动化
- 🔐 **安全可靠**：完整的权限管理和错误处理

## 🏗️ 项目结构

```
aip/
├── android-app/          # Android应用
│   ├── app/             # 应用主模块
│   │   └── src/main/
│   │       ├── java/    # Kotlin源代码
│   │       └── res/     # 资源文件
│   ├── build.gradle.kts # 项目构建配置
│   └── BUILD.md         # 构建说明
│
├── cloud-server/         # 云端服务器
│   ├── src/             # Python源代码
│   │   ├── main.py      # 服务器主文件
│   │   └── task_scheduler.py  # 任务调度器
│   └── requirements.txt # Python依赖
│
├── docs/                 # 文档目录
│   ├── README.md        # 文档索引
│   ├── apk-update-system.md  # APK更新系统
│   ├── design/          # 设计文档
│   │   ├── architecture-design.md
│   │   └── project-structure.md
│   └── archive/         # 归档文档
│       ├── douyin-automation-plan.md
│       └── implementation-plan.md
│
├── plans/                # 开发计划
│   ├── README.md        # 计划索引
│   ├── project-management.md
│   ├── v0.1/            # v0.1版本
│   ├── v0.2/            # v0.2版本
│   └── ...
│
├── tasks/                # 任务配置
│   ├── automation-tasks.md    # 任务定义
│   └── task-system-design.md # 任务系统设计
│
├── scripts/              # 脚本工具
│   ├── auto-install-apk.sh   # APK自动安装（Linux/Mac）
│   ├── auto-install-apk.bat  # APK自动安装（Windows）
│   ├── auto-download-apk.py  # APK自动下载
│   └── README.md             # 脚本说明
│
├── .github/              # GitHub配置
│   └── workflows/
│       └── build-apk.yml     # APK自动构建
│
├── apk-releases/         # APK发布目录
│   └── latest.apk       # 最新版本（符号链接）
│
└── README.md            # 项目说明（本文件）
```

## 🚀 快速开始

### 1. 服务器端

```bash
# 安装依赖
cd cloud-server
pip install -r requirements.txt

# 启动服务器
python3 src/main.py
```

服务器将在 `http://0.0.0.0:50002` 启动

### 2. Android App

#### 方法1：从GitHub Actions下载
1. 访问：https://github.com/mesunrise/aip/actions
2. 下载最新的APK
3. 安装到手机

#### 方法2：本地构建
```bash
cd android-app
./gradlew assembleDebug
```

### 3. 使用App

1. 打开App
2. 开启无障碍服务
3. 连接服务器：`ws://your-server:50002/ws`
4. 点击"开始任务"

## 📚 文档

- [项目管理](plans/project-management.md) - 版本规划和开发进度
- [架构设计](docs/design/architecture-design.md) - 系统架构说明
- [APK更新系统](docs/apk-update-system.md) - 自动更新功能
- [任务系统设计](tasks/task-system-design.md) - 任务调度机制
- [构建说明](android-app/BUILD.md) - Android构建指南
- [脚本工具](scripts/README.md) - 自动化脚本使用

## 🎯 版本规划

### v0.1 - 通信验证 ✅
- WebSocket通信
- 心跳保活
- 消息收发

### v0.2 - 博主定位 🚧
- 无障碍服务
- 抖音启动
- 搜索博主
- 任务控制系统
- APK自动更新

### v0.3 - 评论采集 ⏳
- 进入作品
- 采集评论
- 数据存储

### v0.4 - 私信发送 ⏳
- 私信功能
- 消息模板
- 发送控制

### v0.5 - 优化完善 ⏳
- 性能优化
- 反检测
- 错误处理

### v1.0 - 正式上线 ⏳
- 完整测试
- 文档完善
- 部署上线

## 🛠️ 技术栈

### 服务器端
- Python 3.8+
- FastAPI
- WebSocket
- Uvicorn

### Android端
- Kotlin
- Android SDK 24+
- AccessibilityService
- WebSocket Client
- Coroutines

### 工具链
- GitHub Actions - CI/CD
- Gradle - Android构建
- Git - 版本控制

## 📊 API接口

### WebSocket
```
ws://server:50002/ws
```

### HTTP API
```
GET  /api/tasks/stats      - 任务统计
GET  /api/tasks/{id}       - 任务详情
POST /api/tasks/reload     - 重新加载任务
GET  /api/apk/latest       - 最新APK信息
GET  /api/apk/download     - 下载APK
```

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📄 许可证

MIT License

## 📧 联系方式

- GitHub: https://github.com/mesunrise/aip
- Issues: https://github.com/mesunrise/aip/issues

---

**最后更新：** 2026-05-15  
**当前版本：** v0.2（开发中）
