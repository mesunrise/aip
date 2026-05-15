# APK自动下载和安装

## 📋 功能说明

自动化脚本，用于：
1. 监控GitHub Actions构建状态
2. 构建完成后自动下载APK
3. 自动安装到连接的Android设备
4. 自动启动应用

## 🔧 前置要求

### 1. 安装GitHub CLI
```bash
# macOS
brew install gh

# Windows
winget install GitHub.cli

# Linux
# 参考: https://github.com/cli/cli/blob/trunk/docs/install_linux.md
```

### 2. 登录GitHub
```bash
gh auth login
```

### 3. 安装Android SDK Platform Tools
- 下载：https://developer.android.com/studio/releases/platform-tools
- 确保`adb`命令可用

### 4. 连接Android设备
```bash
# 开启USB调试
# 连接手机到电脑
# 授权USB调试

# 验证连接
adb devices
```

## 🚀 使用方法

### Linux/macOS
```bash
# 赋予执行权限
chmod +x scripts/auto-install-apk.sh

# 运行脚本
./scripts/auto-install-apk.sh
```

### Windows
```cmd
# 直接运行
scripts\auto-install-apk.bat
```

## 📊 执行流程

```
1. 检查依赖 (gh, adb)
   ↓
2. 检查设备连接
   ↓
3. 获取最新构建ID
   ↓
4. 等待构建完成
   ↓
5. 下载APK到 apk-downloads/
   ↓
6. 卸载旧版本
   ↓
7. 安装新版本
   ↓
8. 启动应用
   ↓
9. 完成！
```

## 📝 输出示例

```
🚀 APK自动下载和安装脚本
================================
📋 检查依赖...
✅ 依赖检查通过
📱 检查设备连接...
✅ 检测到 1 个设备
List of devices attached
ABC123456789    device

🔍 获取最新构建...
✅ 找到构建 ID: 12345678
⏳ 等待构建完成...
⏳ 构建中... (状态: in_progress)
⏳ 构建中... (状态: in_progress)
✅ 构建成功！
📥 下载APK...
✅ APK已下载: /path/to/apk-downloads/app-debug.apk
📲 安装APK到设备...
🗑️  卸载旧版本...
📦 安装新版本...
Performing Streamed Install
Success
✅ 安装成功！
🚀 启动应用...
🎉 完成！应用已启动

================================
✅ 全部完成！
APK位置: /path/to/apk-downloads/app-debug.apk
================================
```

## 🔍 故障排除

### 问题1：未检测到设备
```bash
# 检查设备连接
adb devices

# 重启adb服务
adb kill-server
adb start-server

# 检查USB调试是否开启
# 检查是否授权此电脑
```

### 问题2：GitHub CLI未登录
```bash
# 登录GitHub
gh auth login

# 验证登录状态
gh auth status
```

### 问题3：构建失败
```bash
# 查看构建日志
gh run view <RUN_ID> --repo mesunrise/aip --log

# 在浏览器中查看
gh run view <RUN_ID> --repo mesunrise/aip --web
```

### 问题4：安装失败
```bash
# 检查设备存储空间
adb shell df

# 手动安装
adb install -r apk-downloads/app-debug.apk

# 查看详细错误
adb install -r -d apk-downloads/app-debug.apk
```

## 🎯 高级用法

### 监控特定构建
```bash
# 修改脚本中的RUN_ID
RUN_ID=12345678
```

### 安装到特定设备
```bash
# 查看所有设备
adb devices

# 指定设备安装
adb -s ABC123456789 install -r app-debug.apk
```

### 自动化测试
```bash
# 安装后自动运行测试
./scripts/auto-install-apk.sh && adb shell am instrument -w com.douyin.automation.test/androidx.test.runner.AndroidJUnitRunner
```

## 📦 集成到CI/CD

### GitHub Actions
```yaml
- name: Download and Install APK
  run: |
    ./scripts/auto-install-apk.sh
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

### 本地开发流程
```bash
# 1. 修改代码
git add .
git commit -m "feat: 新功能"
git push

# 2. 自动下载并安装
./scripts/auto-install-apk.sh

# 3. 测试
adb logcat | grep "DouyinAutomation"
```

## 🔐 安全注意事项

1. **GitHub Token**：脚本使用`gh` CLI，需要有效的GitHub认证
2. **设备授权**：确保只在授权的设备上运行
3. **APK签名**：debug版本使用debug签名，不要用于生产环境

## 📚 相关文档

- [GitHub CLI文档](https://cli.github.com/manual/)
- [ADB文档](https://developer.android.com/studio/command-line/adb)
- [GitHub Actions Artifacts](https://docs.github.com/en/actions/using-workflows/storing-workflow-data-as-artifacts)

---

**最后更新：** 2026-05-15  
**维护者：** 开发团队
