# 抖音自动化 Android App

## v0.1 - 通信验证

### 功能
- WebSocket客户端
- 连接云端服务器
- 消息收发
- 心跳保活
- 断线重连

### 技术栈
- Kotlin
- OkHttp WebSocket
- Coroutines

### 项目结构
```
app/src/main/java/com/douyin/automation/
├── MainActivity.kt           # 主界面
├── network/
│   ├── WebSocketClient.kt   # WebSocket客户端
│   └── MessageHandler.kt    # 消息处理
└── model/
    └── Message.kt           # 消息模型
```

### 快速开始

1. 用Android Studio打开项目
2. 修改`WebSocketClient.kt`中的服务器地址
3. 运行到设备
4. 点击"连接"按钮

### 配置

```kotlin
// WebSocketClient.kt
private val serverUrl = "ws://your-server:8000/ws"
```
