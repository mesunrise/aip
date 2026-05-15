#!/bin/bash

# 本地构建APK脚本
# 用于在无法推送到GitHub时本地构建

echo "🔨 开始本地构建APK..."

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未找到Java环境"
    echo "请安装JDK 17或更高版本"
    exit 1
fi

# 进入android-app目录
cd "$(dirname "$0")/android-app" || exit 1

# 清理旧的构建
echo "🧹 清理旧的构建..."
./gradlew clean

# 构建Debug APK
echo "📦 构建Debug APK..."
./gradlew assembleDebug

# 检查构建结果
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "✅ 构建成功!"
    echo "📱 APK位置: android-app/app/build/outputs/apk/debug/app-debug.apk"
    
    # 显示APK信息
    ls -lh app/build/outputs/apk/debug/app-debug.apk
    
    # 复制到项目根目录
    cp app/build/outputs/apk/debug/app-debug.apk ../app-debug.apk
    echo "📋 已复制到: app-debug.apk"
else
    echo "❌ 构建失败"
    exit 1
fi
