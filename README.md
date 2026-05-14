# 手机自动化项目 v0.1

## 🎉 项目已启动！

### 项目结构

```
douyin-automation/
├── android-app/          # Android应用（Kotlin）
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/douyin/automation/
│   │   │   │   ├── MainActivity.kt              # 主界面
│   │   │   │   └── network/
│   │   │   │       └── WebSocketClient.kt       # WebSocket客户端
│   │   │   ├── res/layout/
│   │   │   │   └── activity_main.xml            # UI布局
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle.kts
│   └── README.md
│
├── cloud-server/         # 云端服务器（Python）
│   ├── src/
│   │   └── main.py                              # 服务器入口
│   ├── requirements.txt
│   └── README.md
│
├── docs/                 # 技术文档
├── plans/                # 开发计划
├── SETUP.md             # 环境配置指南
├── VIBE_CODING.md       # 开发规范
└── PROJECT_STATUS.md    # 项目状态
```

## 🚀 快速开始

### 1. 启动云端服务器

```bash
# 安装依赖
cd cloud-server
pip install -r requirements.txt

# 启动服务器
python src/main.py
```

服务器将在以下地址启动：
- WebSocket: `ws://localhost:8000/ws/{device_id}`
- Web控制台: `http://localhost:8000`

### 2. 运行Android App

```bash
# 用Android Studio打开项目
android-app/

# 或使用命令行
cd android-app
./gradlew installDebug
```

**配置服务器地址：**
- 模拟器: `ws://10.0.2.2:8000/ws/android_001`
- 真机: `ws://你的电脑IP:8000/ws/android_001`

### 3. 测试连接

1. 启动服务器
2. 打开App，点击"连接"
3. 在App中发送消息
4. 在Web控制台发送消息给设备

## ✅ v0.1 功能清单

### Android App
- [x] WebSocket客户端
- [x] 连接/断开功能
- [x] 消息收发
- [x] 心跳保活（30秒）
- [x] 简单UI界面

### 云端服务器
- [x] WebSocket服务器
- [x] 设备连接管理
- [x] 消息路由
- [x] 心跳响应
- [x] Web控制台

## 📋 验收测试

运行以下测试确保v0.1功能正常：

```bash
# 测试1: 服务器启动
python cloud-server/src/main.py
# 预期: 看到启动信息

# 测试2: Web控制台
# 浏览器访问 http://localhost:8000
# 预期: 看到控制台页面

# 测试3: App连接
# 在App中点击"连接"
# 预期: 状态显示"已连接"

# 测试4: 消息发送
# App发送消息 "Hello Server"
# 预期: 服务器控制台显示收到消息

# 测试5: 消息接收
# Web控制台发送消息给设备
# 预期: App显示收到消息
```

## 📖 文档

- [环境配置](SETUP.md) - 详细的安装和配置步骤
- [开发规范](VIBE_CODING.md) - Vibe Coding最佳实践
- [项目状态](PROJECT_STATUS.md) - 当前进度和待办事项
- [开发计划](plans/development-roadmap.md) - 完整的迭代计划

## 🎯 下一步：v0.2

v0.1完成后，将进入v0.2开发：
- 实现无障碍服务
- 实现抖音App启动
- 实现博主搜索功能

预计时间：1周

## 💡 技术栈

**Android:**
- Kotlin
- OkHttp WebSocket
- Coroutines

**服务器:**
- Python 3.9+
- FastAPI
- WebSocket

## 🆘 常见问题

**Q: App连接失败？**
A: 检查服务器地址，模拟器用`10.0.2.2`，真机用电脑IP

**Q: 如何查看电脑IP？**
```bash
# Windows
ipconfig

# Mac/Linux
ifconfig
```

**Q: 端口被占用？**
```bash
# 修改 cloud-server/src/main.py 中的端口
uvicorn.run(app, host="0.0.0.0", port=8001)  # 改为8001
```

---

**项目已准备就绪！开始测试v0.1功能吧！** 🎉
