# 开放防火墙端口指南

## 🚨 问题说明

**当前状态：**
- ✅ 服务器运行正常（80端口）
- ✅ 本地连接成功
- ❌ 外网访问被阻止（防火墙）

**原因：**
- Bohrium云服务器的防火墙由云平台控制
- 系统内部无法修改（没有iptables）
- 必须通过Bohrium控制台配置

## 📋 方案1：Bohrium控制台开放端口（推荐）

### 步骤1：登录控制台

1. **访问Bohrium控制台**
   ```
   https://bohrium.dp.tech
   ```

2. **使用您的账号登录**

### 步骤2：找到服务器

1. **进入"计算资源"或"实例管理"**

2. **找到您的服务器**
   - 服务器1：elxn1431783.bohrium.tech
   - 服务器2：wdzd1450232.bohrium.tech

### 步骤3：配置安全组

1. **点击服务器名称进入详情**

2. **找到"安全组"或"防火墙"选项**
   - 可能在"网络"、"安全"或"配置"标签下

3. **添加入站规则**
   ```
   规则类型：自定义TCP
   协议：TCP
   端口范围：80
   来源：0.0.0.0/0（所有IP）
   动作：允许
   描述：WebSocket服务
   ```

4. **（可选）同时开放8080端口**
   ```
   规则类型：自定义TCP
   协议：TCP
   端口范围：8080
   来源：0.0.0.0/0
   动作：允许
   描述：WebSocket备用端口
   ```

### 步骤4：保存并验证

1. **保存配置**
   - 点击"保存"或"应用"

2. **等待生效**（1-2分钟）

3. **验证端口开放**
   ```bash
   curl http://elxn1431783.bohrium.tech/
   ```

4. **如果看到JSON响应，说明成功！**
   ```json
   {"service":"抖音自动化云端服务器","version":"0.1.0","status":"running"}
   ```

### 步骤5：测试App连接

1. **打开App**

2. **输入服务器地址**
   ```
   ws://elxn1431783.bohrium.tech/ws
   ```

3. **点击"连接"**

4. **测试功能**

## 📋 方案2：联系Bohrium技术支持

### 如果找不到安全组设置

**发送工单或邮件：**

**主题：** 请求开放服务器端口

**内容：**
```
您好，

我需要开放以下服务器的端口用于WebSocket服务：

服务器信息：
- 域名：elxn1431783.bohrium.tech
- 或：wdzd1450232.bohrium.tech

需要开放的端口：
- 端口：80 TCP
- 来源：0.0.0.0/0（所有IP）
- 用途：WebSocket服务

请协助开放，谢谢！

用户：[您的用户名]
```

**联系方式：**
- 工单系统：Bohrium控制台 → 帮助 → 提交工单
- 邮箱：support@dp.tech（如果有）
- 在线客服：控制台右下角

## 📋 方案3：使用ngrok内网穿透（临时方案）

### 如果无法立即开放端口，使用ngrok临时测试

### 步骤1：安装ngrok

```bash
cd /root
wget https://bin.equinox.io/c/bMaiKPlPvHa/ngrok-v3-stable-linux-amd64.tgz
tar -xzf ngrok-v3-stable-linux-amd64.tgz
chmod +x ngrok
```

### 步骤2：注册ngrok账号

1. **访问ngrok官网**
   ```
   https://dashboard.ngrok.com/signup
   ```

2. **注册免费账号**
   - 使用邮箱注册
   - 验证邮箱

3. **获取authtoken**
   - 登录后访问：https://dashboard.ngrok.com/get-started/your-authtoken
   - 复制您的authtoken

### 步骤3：配置ngrok

```bash
./ngrok config add-authtoken YOUR_AUTH_TOKEN
```

### 步骤4：启动ngrok隧道

```bash
# 在新终端运行
./ngrok http 80
```

**输出示例：**
```
ngrok

Session Status: online
Forwarding: https://1234-56-78-90-123.ngrok-free.app -> http://localhost:80

Connections: 0
```

### 步骤5：使用ngrok地址

**复制Forwarding地址，例如：**
```
https://1234-56-78-90-123.ngrok-free.app
```

**浏览器测试：**
```
https://1234-56-78-90-123.ngrok-free.app/test
```

**App连接地址：**
```
wss://1234-56-78-90-123.ngrok-free.app/ws
```

**注意：** 使用wss://（HTTPS的WebSocket）

### ngrok优缺点

**优点：**
- ✅ 立即可用
- ✅ 无需配置防火墙
- ✅ 免费版够用
- ✅ 支持HTTPS

**缺点：**
- ❌ 地址会变（重启后）
- ❌ 免费版有连接限制
- ❌ 依赖第三方服务

## 📊 方案对比

| 方案 | 难度 | 速度 | 稳定性 | 推荐度 |
|------|------|------|--------|--------|
| Bohrium控制台 | 简单 | 快 | 高 | ⭐⭐⭐⭐⭐ |
| 联系技术支持 | 简单 | 慢 | 高 | ⭐⭐⭐⭐ |
| ngrok | 简单 | 快 | 中 | ⭐⭐⭐ |

## 🎯 推荐流程

### 立即执行：

1. **尝试在Bohrium控制台开放端口**
   - 最快最稳定的方案

2. **如果找不到设置，联系技术支持**
   - 提交工单请求开放端口

3. **同时使用ngrok进行临时测试**
   - 验证App功能
   - 完成v0.1验收

4. **端口开放后切换到直连**
   - 更稳定
   - 无需依赖第三方

## 🧪 验证端口是否开放

### 方法1：curl测试

```bash
curl http://elxn1431783.bohrium.tech/
```

**成功响应：**
```json
{"service":"抖音自动化云端服务器","version":"0.1.0","status":"running"}
```

**失败响应：**
```
curl: (28) Connection timed out
```

### 方法2：浏览器测试

**访问：**
```
http://elxn1431783.bohrium.tech/test
```

**成功：** 看到测试页面
**失败：** 连接超时

### 方法3：在线端口检测

**使用在线工具：**
- https://www.yougetsignal.com/tools/open-ports/
- 输入：elxn1431783.bohrium.tech
- 端口：80

## 📝 常见问题

### Q1：找不到安全组设置？

**A：** 联系Bohrium技术支持，他们会协助开放端口。

### Q2：开放端口需要多久？

**A：** 通常1-2分钟生效，最多5分钟。

### Q3：ngrok免费版够用吗？

**A：** 够用于测试，但长期使用建议开放防火墙端口。

### Q4：可以开放其他端口吗？

**A：** 可以，推荐80（HTTP标准端口）或8080（常用Web端口）。

## 🚀 快速开始

### 最快的测试方法（ngrok）

```bash
# 1. 下载ngrok
cd /root
wget https://bin.equinox.io/c/bMaiKPlPvHa/ngrok-v3-stable-linux-amd64.tgz
tar -xzf ngrok-v3-stable-linux-amd64.tgz

# 2. 注册并获取token
# 访问 https://dashboard.ngrok.com/signup

# 3. 配置
./ngrok config add-authtoken YOUR_TOKEN

# 4. 启动
./ngrok http 80

# 5. 复制Forwarding地址
# 例如：https://1234-56-78-90-123.ngrok-free.app

# 6. App连接
# wss://1234-56-78-90-123.ngrok-free.app/ws
```

---

## 总结

**开放防火墙端口的方法：**
1. ✅ Bohrium控制台配置（最佳）
2. ✅ 联系技术支持（简单）
3. ✅ 使用ngrok（临时）

**推荐：先用ngrok测试，同时申请开放端口！**
