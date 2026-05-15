# APK自动更新系统

## 📋 系统概述

实现APK的自动下载和更新功能：
1. 服务器自动下载最新APK
2. App内置更新按钮
3. 一键下载并安装

## 🏗️ 系统架构

```
GitHub Actions
     ↓ 构建完成
服务器定时检查
     ↓ 下载APK
/apk-releases/latest.apk
     ↓ HTTP API
App点击更新按钮
     ↓ 下载APK
自动安装
```

## 🔧 服务器端配置

### 1. 自动下载脚本
**文件：** `scripts/auto-download-apk.sh`

**功能：**
- 检查最新构建
- 下载APK到服务器
- 创建latest.apk链接
- 清理旧版本

**使用：**
```bash
# 手动执行
./scripts/auto-download-apk.sh

# 定时任务（每5分钟检查一次）
crontab -e
*/5 * * * * /personal/ai_workspace/aip/scripts/auto-download-apk.sh >> /var/log/apk-download.log 2>&1
```

### 2. HTTP API
**文件：** `cloud-server/src/main.py`

**接口：**
```
GET /api/apk/latest      - 获取最新APK信息
GET /api/apk/download    - 下载最新APK
```

**示例：**
```bash
# 获取APK信息
curl http://elxn1431783.bohrium.tech:50002/api/apk/latest

# 下载APK
curl -O http://elxn1431783.bohrium.tech:50002/api/apk/download
```

## 📱 App端实现

### 1. 添加更新按钮
**文件：** `activity_main.xml`

```xml
<Button
    android:id="@+id/updateBtn"
    android:text="🔄 检查更新"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

### 2. 实现更新功能
**文件：** `MainActivity.kt`

```kotlin
// 检查更新
fun checkUpdate() {
    // 1. 请求 /api/apk/latest
    // 2. 显示版本信息
    // 3. 询问是否更新
}

// 下载APK
fun downloadApk(url: String) {
    // 1. 使用DownloadManager下载
    // 2. 显示下载进度
    // 3. 下载完成后安装
}

// 安装APK
fun installApk(file: File) {
    // 1. 请求安装权限
    // 2. 调用系统安装器
}
```

### 3. 权限配置
**文件：** `AndroidManifest.xml`

```xml
<!-- 下载权限 -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

<!-- 安装权限 -->
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
```

## 🚀 使用流程

### 服务器端
```bash
# 1. 设置定时任务
crontab -e
*/5 * * * * /personal/ai_workspace/aip/scripts/auto-download-apk.sh

# 2. 手动触发一次
./scripts/auto-download-apk.sh

# 3. 验证APK存在
ls -lh /personal/ai_workspace/aip/apk-releases/
```

### App端
```
1. 打开App
2. 点击"🔄 检查更新"
3. 查看版本信息
4. 点击"立即更新"
5. 等待下载完成
6. 自动跳转安装
7. 安装完成后重启App
```

## 📊 API响应示例

### GET /api/apk/latest
```json
{
  "version": "latest",
  "filename": "app-debug-20260515_133000-12345678.apk",
  "size": 5242880,
  "size_mb": 5.0,
  "modified": 1715760000.0,
  "download_url": "http://elxn1431783.bohrium.tech:50002/api/apk/download"
}
```

### GET /api/apk/download
```
Content-Type: application/vnd.android.package-archive
Content-Disposition: attachment; filename=app-debug.apk
Content-Length: 5242880

[APK文件内容]
```

## 🔍 故障排除

### 问题1：服务器下载失败
```bash
# 检查GitHub CLI
gh auth status

# 手动下载测试
gh run list --repo mesunrise/aip --workflow build-apk.yml --limit 1
```

### 问题2：App下载失败
```bash
# 检查服务器APK
ls -lh /personal/ai_workspace/aip/apk-releases/

# 检查API
curl http://elxn1431783.bohrium.tech:50002/api/apk/latest
```

### 问题3：安装失败
```
1. 检查"未知来源"权限
2. 检查存储权限
3. 检查安装权限
4. 查看logcat日志
```

## 🎯 优势

### 之前：
```
1. 访问GitHub Actions
2. 等待构建完成
3. 下载APK到电脑
4. 传输到手机
5. 手动安装
⏱️ 耗时：5-10分钟
```

### 现在：
```
1. 点击"检查更新"
2. 点击"立即更新"
3. 自动下载安装
⏱️ 耗时：1-2分钟
🎉 完全自动化！
```

## 📝 安全注意事项

1. **HTTPS**：生产环境应使用HTTPS
2. **签名验证**：验证APK签名
3. **版本检查**：比较版本号避免降级
4. **权限控制**：限制API访问

---

**最后更新：** 2026-05-15  
**维护者：** 开发团队
