# 📜 脚本说明

## 📥 APK下载脚本

### download-apk.py
从GitHub Actions下载最新成功构建的APK到本地。

**使用方法：**
```bash
# 设置GitHub Token
export GITHUB_TOKEN=your_github_personal_access_token

# 下载APK
python3 scripts/download-apk.py
```

**输出：**
- APK保存到：`apk-releases/latest.apk`
- 同时保留原始文件：`apk-releases/app-debug.apk`

### download-apk.sh
Bash版本的下载脚本（功能相同）。

**使用方法：**
```bash
export GITHUB_TOKEN=your_token
bash scripts/download-apk.sh
```

### auto-download-apk.sh
自动监控GitHub Actions并下载新构建的APK。

**使用方法：**
```bash
# 后台运行
export GITHUB_TOKEN=your_token
nohup bash scripts/auto-download-apk.sh > apk-download.log 2>&1 &

# 查看日志
tail -f apk-download.log
```

**功能：**
- 每5分钟检查一次新构建
- 发现新构建自动下载
- 保存到`apk-releases/latest.apk`

## 🔍 构建监控脚本

### monitor-build.py
实时监控GitHub Actions构建状态。

**使用方法：**
```bash
python3 scripts/monitor-build.py
```

### monitor-build.sh
Bash版本的监控脚本。

**使用方法：**
```bash
bash scripts/monitor-build.sh
```

## 🔑 获取GitHub Token

1. 访问：https://github.com/settings/tokens
2. 点击"Generate new token (classic)"
3. 选择权限：
   - `repo` (完整仓库访问)
   - `workflow` (访问GitHub Actions)
4. 生成并复制token
5. 设置环境变量：
   ```bash
   export GITHUB_TOKEN=your_token_here
   
   # 永久保存（添加到~/.bashrc或~/.zshrc）
   echo 'export GITHUB_TOKEN=your_token_here' >> ~/.bashrc
   source ~/.bashrc
   ```

## 📋 使用示例

### 手动下载最新APK
```bash
# 1. 设置token
export GITHUB_TOKEN=ghp_xxxxxxxxxxxx

# 2. 下载APK
python3 scripts/download-apk.py

# 3. 查看下载的APK
ls -lh apk-releases/latest.apk
```

### 自动监控并下载
```bash
# 1. 设置token
export GITHUB_TOKEN=ghp_xxxxxxxxxxxx

# 2. 后台运行监控
nohup bash scripts/auto-download-apk.sh > apk-download.log 2>&1 &

# 3. 查看日志
tail -f apk-download.log

# 4. 停止监控
pkill -f auto-download-apk.sh
```

### 集成到服务器启动
```bash
# 在服务器启动脚本中添加
export GITHUB_TOKEN=your_token
nohup bash scripts/auto-download-apk.sh > apk-download.log 2>&1 &
cd cloud-server && python3 src/main.py
```

## 🎯 工作流程

```
GitHub Actions构建APK
         ↓
    上传到Artifacts
         ↓
  auto-download-apk.sh监控
         ↓
    发现新构建
         ↓
  download-apk.py下载
         ↓
  保存到apk-releases/latest.apk
         ↓
  服务器提供下载
```

## 📱 服务器端APK下载API

服务器已配置APK下载接口：

```bash
# 获取APK信息
curl http://elxn1431783.bohrium.tech:50002/api/apk/latest

# 下载APK
curl -O http://elxn1431783.bohrium.tech:50002/api/apk/download
```

## 🔧 故障排除

### 问题1：未设置GITHUB_TOKEN
```
⚠️  未设置GITHUB_TOKEN环境变量
```
**解决：** 设置GitHub Personal Access Token

### 问题2：Token权限不足
```
❌ 网络错误: 403 Forbidden
```
**解决：** 确保token有`repo`和`workflow`权限

### 问题3：未找到成功的构建
```
❌ 未找到成功的构建
```
**解决：** 等待GitHub Actions构建完成

### 问题4：下载失败
```
❌ APK下载失败
```
**解决：** 检查网络连接和token有效性

## 📊 日志说明

### 下载日志
```
📥 从GitHub Actions下载APK
================================
🔍 查找最新成功的构建...
✅ 找到构建: Run #30 (ID: 25907100805)
   Commit: fix: 移除多余的ScrollView闭合标签
📦 获取artifacts...
✅ 找到artifact: app-debug (ID: 1234567)
⬇️  下载artifact...
✅ 下载完成
📦 解压APK...
✅ APK已保存到: apk-releases/latest.apk

📊 文件信息:
  路径: apk-releases/latest.apk
  大小: 5.23 MB

🎉 下载完成！
```

### 监控日志
```
🤖 启动APK自动下载监控
================================
仓库: mesunrise/aip
检查间隔: 300秒
APK目录: apk-releases

[2026-05-15 16:00:00] 🔍 检查新构建...
[2026-05-15 16:00:01] 🎉 发现新构建: Run ID 25907100805
[2026-05-15 16:00:01] 📥 开始下载APK...
[2026-05-15 16:00:05] ✅ APK下载成功
[2026-05-15 16:00:05] ⏰ 等待300秒...
```
