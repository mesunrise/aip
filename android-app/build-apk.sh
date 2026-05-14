#!/bin/bash

echo "🔨 开始构建APK..."

# 清理旧的构建
./gradlew clean

# 构建Debug APK
./gradlew assembleDebug

# 检查构建结果
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "✅ 构建成功！"
    
    # 复制到项目根目录
    cp app/build/outputs/apk/debug/app-debug.apk ../douyin-automation-v0.1-debug.apk
    
    echo "📦 APK位置："
    echo "   - app/build/outputs/apk/debug/app-debug.apk"
    echo "   - ../douyin-automation-v0.1-debug.apk"
    
    # 显示APK信息
    ls -lh app/build/outputs/apk/debug/app-debug.apk
else
    echo "❌ 构建失败！"
    exit 1
fi
