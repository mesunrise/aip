# 抖音自动化云端服务器

## v0.1 - 通信验证

### 功能
- WebSocket服务器
- 设备连接管理
- 消息路由
- 心跳检测
- Web控制台

### 技术栈
- Python 3.9+
- FastAPI
- WebSocket

### 项目结构
```
src/
├── main.py                  # 入口
├── websocket/
│   ├── server.py           # WebSocket服务器
│   └── device_manager.py   # 设备管理
├── models/
│   └── message.py          # 消息模型
└── web/
    └── console.py          # Web控制台
```

### 快速开始

```bash
# 安装依赖
pip install -r requirements.txt

# 运行服务器
python src/main.py

# 访问控制台
http://localhost:8000
```

### API

#### WebSocket连接
```
ws://localhost:8000/ws/{device_id}
```

#### 消息格式
```json
{
  "type": "heartbeat",
  "deviceId": "device_001",
  "timestamp": 1234567890
}
```
