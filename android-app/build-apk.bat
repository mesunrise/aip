@echo off
echo 🔨 开始构建APK...

REM 清理旧的构建
call gradlew.bat clean

REM 构建Debug APK
call gradlew.bat assembleDebug

REM 检查构建结果
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo ✅ 构建成功！
    
    REM 复制到项目根目录
    copy app\build\outputs\apk\debug\app-debug.apk ..\douyin-automation-v0.1-debug.apk
    
    echo 📦 APK位置：
    echo    - app\build\outputs\apk\debug\app-debug.apk
    echo    - ..\douyin-automation-v0.1-debug.apk
    
    dir app\build\outputs\apk\debug\app-debug.apk
) else (
    echo ❌ 构建失败！
    exit /b 1
)
