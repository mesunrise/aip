# 环境配置指南

## v0.1 - 通信验证

### 1. 云端服务器配置

#### 安装Python
```bash
# 确保Python 3.9+
python --version
```

#### 安装依赖
```bash
cd cloud-server
pip install -r requirements.txt
```

#### 启动服务器
```bash
python src/main.py
```

服务器将在 `http://localhost:8000` 启动

### 2. Android App配置

#### 安装Android Studio
- 下载: https://developer.android.com/studio
- 版本: 2023.1.1+

#### 打开项目
```bash
# 用Android Studio打开
android-app/
```

#### 配置服务器地址

在 `MainActivity.kt` 中修改默认地址：

```kotlin
// 本地测试（模拟器）
serverUrlInput.setText("ws://10.0.2.2:8000/ws/android_001")

// 真机测试（替换为电脑IP）
serverUrlInput.setText("ws://192.168.1.100:8000/ws/android_001")
```

#### 运行App
1. 连接Android设备或启动模拟器
2. 点击 Run 按钮
3. 等待安装完成

### 3. 测试连接

#### 步骤1：启动服务器
```bash
cd cloud-server
python src/main.py
```

看到输出：
```
🚀 启动服务器...
📱 WebSocket: ws://localhost:8000/ws/{device_id}
🌐 控制台: http://localhost:8000
```

#### 步骤2：打开Web控制台
浏览器访问: http://localhost:8000

#### 步骤3：运行Android App
1. 打开App
2. 点击"连接"按钮
3. 查看状态变为"已连接"

#### 步骤4：测试消息
在App中：
- 输入消息："Hello Server"
- 点击"发送消息"
- 查看日志中的回复

在Web控制台中：
- 输入设备ID："android_001"
- 输入消息："Hello App"
- 点击"发送"
- App应该收到消息

### 4. 验收标准

✅ 服务器成功启动
✅ App成功连接
✅ App能发送消息
✅ 服务器能接收消息
✅ 服务器能发送消息
✅ App能接收消息
✅ 心跳正常工作（30秒一次）
✅ 断网后能看到断开提示

### 5. 常见问题

#### Q: App连接失败
A: 检查服务器地址是否正确
- 模拟器使用 `10.0.2.2`
- 真机使用电脑的局域网IP

#### Q: 找不到电脑IP
```bash
# Windows
ipconfig

# Mac/Linux
ifconfig
```

#### Q: 服务器启动失败
A: 检查端口8000是否被占用
```bash
# Windows
netstat -ano | findstr :8000

# Mac/Linux
lsof -i :8000
```

### 6. 下一步

v0.1完成后，进入v0.2开发：
- 实现无障碍服务
- 实现抖音操作
- 实现博主搜索

查看 [`plans/development-roadmap.md`](plans/development-roadmap.md) 了解详细计划。
