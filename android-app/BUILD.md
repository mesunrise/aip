# 构建APK安装包

## 方法1：使用Android Studio（推荐）

1. 打开Android Studio
2. 打开项目 `android-app/`
3. 菜单：Build → Build Bundle(s) / APK(s) → Build APK(s)
4. 等待构建完成
5. 点击通知中的 "locate" 链接

**APK位置：**
```
android-app/app/build/outputs/apk/debug/app-debug.apk
```

## 方法2：使用命令行

### Windows
```bash
cd android-app
gradlew.bat assembleDebug
```

### Mac/Linux
```bash
cd android-app
./gradlew assembleDebug
```

**APK位置：**
```
android-app/app/build/outputs/apk/debug/app-debug.apk
```

## 安装APK

### 方法1：通过ADB安装
```bash
adb install android-app/app/build/outputs/apk/debug/app-debug.apk
```

### 方法2：直接传输到手机
1. 将APK文件传输到手机
2. 在手机上打开文件管理器
3. 点击APK文件安装
4. 允许"未知来源"安装

## 构建Release版本

### 1. 生成签名密钥
```bash
keytool -genkey -v -keystore douyin-automation.keystore -alias douyin -keyalg RSA -keysize 2048 -validity 10000
```

### 2. 配置签名
在 `android-app/app/build.gradle.kts` 中添加：
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../douyin-automation.keystore")
            storePassword = "your_password"
            keyAlias = "douyin"
            keyPassword = "your_password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### 3. 构建Release APK
```bash
./gradlew assembleRelease
```

**Release APK位置：**
```
android-app/app/build/outputs/apk/release/app-release.apk
```

## 快速构建脚本

我已经创建了快速构建脚本：

### Windows
```bash
build-apk.bat
```

### Mac/Linux
```bash
./build-apk.sh
```

构建完成后，APK会自动复制到项目根目录：
```
douyin-automation-v0.1-debug.apk
```

## 注意事项

1. **Debug版本**
   - 用于开发测试
   - 包含调试信息
   - 体积较大
   - 不需要签名

2. **Release版本**
   - 用于正式发布
   - 代码混淆优化
   - 体积较小
   - 需要签名

3. **首次构建**
   - 需要下载Gradle和依赖
   - 可能需要10-20分钟
   - 后续构建会快很多

## 故障排查

### 问题1：Gradle下载慢
```bash
# 使用国内镜像
# 在 android-app/build.gradle.kts 中添加：
repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/google") }
    google()
    mavenCentral()
}
```

### 问题2：构建失败
```bash
# 清理项目
./gradlew clean

# 重新构建
./gradlew assembleDebug
```

### 问题3：找不到gradlew
```bash
# 下载Gradle Wrapper
gradle wrapper
```
