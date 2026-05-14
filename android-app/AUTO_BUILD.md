# GitHub Actions 自动构建APK

## 🚀 自动化构建方案

使用GitHub Actions实现完全自动化的APK构建，无需本地安装Android SDK。

## 📋 工作流程

```
推送代码 → GitHub Actions自动构建 → 下载APK
```

## ⚙️ 配置步骤

### 1. 创建GitHub仓库

```bash
# 初始化Git仓库
cd /personal/ai_workspace/aip
git init
git add .
git commit -m "feat: v0.1 初始版本"

# 关联GitHub仓库
git remote add origin https://github.com/mesunrise/aip.git
git branch -M main
git push -u origin main
```

### 2. GitHub Actions配置

配置文件已创建：[`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml)

**触发条件：**
- 推送到 `main` 或 `develop` 分支
- 创建Pull Request
- 手动触发

### 3. 推送代码触发构建

```bash
# 修改代码后
git add .
git commit -m "feat: 0.1版本构建"
git push

# GitHub Actions会自动开始构建
```

### 4. 下载构建的APK

1. 访问GitHub仓库
2. 点击 **Actions** 标签
3. 选择最新的构建任务
4. 在 **Artifacts** 区域下载APK

**APK文件名：**
- `app-debug.apk` - Debug版本
- `app-release.apk` - Release版本（需要配置签名）

## 📦 构建产物

### Debug版本
- 文件名：`app-debug.apk`
- 用途：开发测试
- 特点：包含调试信息，体积较大

### Release版本
- 文件名：`app-release.apk`
- 用途：正式发布
- 特点：代码混淆，体积较小
- 需要：配置签名密钥

## 🔐 配置Release签名（可选）

### 1. 生成签名密钥

```bash
keytool -genkey -v -keystore douyin-automation.keystore \
  -alias douyin -keyalg RSA -keysize 2048 -validity 10000
```

### 2. 转换为Base64

```bash
base64 douyin-automation.keystore > keystore.base64
```

### 3. 在GitHub添加Secrets

仓库 → Settings → Secrets and variables → Actions → New repository secret

添加以下Secrets：
- `KEYSTORE_FILE`: keystore.base64的内容
- `KEYSTORE_PASSWORD`: 密钥库密码
- `KEY_ALIAS`: douyin
- `KEY_PASSWORD`: 密钥密码

### 4. 更新build.gradle.kts

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
        }
    }
}
```

## 📊 构建状态

在README.md中添加构建徽章：

```markdown
![Build Status](https://github.com/你的用户名/douyin-automation/workflows/Build%20Android%20APK/badge.svg)
```

## 🎯 使用场景

### 场景1：开发测试
```bash
# 修改代码
git add .
git commit -m "feat: 新功能"
git push

# 等待构建完成（约5-10分钟）
# 下载app-debug.apk测试
```

### 场景2：版本发布
```bash
# 创建版本标签
git tag v0.1.0
git push origin v0.1.0

# 自动构建并创建Release
# 下载app-release.apk发布
```

### 场景3：Pull Request测试
```bash
# 创建功能分支
git checkout -b feature/new-feature
git push origin feature/new-feature

# 创建PR后自动构建测试
```

## ⏱️ 构建时间

- **首次构建：** 约10-15分钟（下载依赖）
- **后续构建：** 约5-8分钟（使用缓存）

## 💰 费用

GitHub Actions对公开仓库**完全免费**！

私有仓库每月有2000分钟免费额度。

## 🔍 查看构建日志

1. 访问Actions页面
2. 点击构建任务
3. 查看详细日志
4. 排查构建错误

## ✅ 验证构建

构建完成后，检查：
- ✅ 构建状态为绿色（成功）
- ✅ Artifacts中有APK文件
- ✅ APK大小合理（5-10MB）
- ✅ 可以正常安装

## 🆘 常见问题

### Q: 构建失败怎么办？
A: 查看构建日志，通常是依赖问题或代码错误

### Q: 找不到APK？
A: 在Actions页面的Artifacts区域下载

### Q: 构建太慢？
A: 首次构建需要下载依赖，后续会快很多

### Q: 如何构建Release版本？
A: 配置签名密钥，修改workflow文件

## 📚 相关文档

- [GitHub Actions文档](https://docs.github.com/en/actions)
- [Android构建指南](BUILD.md)
- [项目开发计划](../plans/development-roadmap.md)

---

**准备好了吗？推送代码到GitHub，开始自动构建！** 🚀
