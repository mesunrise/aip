@echo off
REM APK自动下载和安装脚本 (Windows版本)
REM 功能：监控GitHub Actions构建，完成后自动下载APK并安装到手机

setlocal enabledelayedexpansion

set REPO=mesunrise/aip
set WORKFLOW_NAME=build-apk.yml
set APK_NAME=app-debug.apk
set DOWNLOAD_DIR=apk-downloads

echo ========================================
echo 🚀 APK自动下载和安装脚本
echo ========================================
echo.

REM 检查GitHub CLI
where gh >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ 未安装 GitHub CLI (gh)
    echo 请从 https://cli.github.com/ 下载安装
    exit /b 1
)

REM 检查ADB
where adb >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ 未安装 ADB
    echo 请安装 Android SDK Platform Tools
    exit /b 1
)

echo ✅ 依赖检查通过
echo.

REM 检查设备
echo 📱 检查设备连接...
adb devices | findstr "device$" >nul
if %errorlevel% neq 0 (
    echo ❌ 未检测到Android设备
    echo 请确保：
    echo   1. 手机已通过USB连接
    echo   2. 已开启USB调试
    echo   3. 已授权此电脑
    exit /b 1
)

echo ✅ 设备已连接
adb devices
echo.

REM 获取最新构建
echo 🔍 获取最新构建...
for /f "delims=" %%i in ('gh run list --repo %REPO% --workflow %WORKFLOW_NAME% --limit 1 --json databaseId --jq ".[0].databaseId"') do set RUN_ID=%%i

if "%RUN_ID%"=="" (
    echo ❌ 未找到构建记录
    exit /b 1
)

echo ✅ 找到构建 ID: %RUN_ID%
echo.

REM 等待构建完成
echo ⏳ 等待构建完成...
:wait_loop
for /f "delims=" %%i in ('gh run view %RUN_ID% --repo %REPO% --json status --jq ".status"') do set STATUS=%%i

if "%STATUS%"=="completed" (
    for /f "delims=" %%i in ('gh run view %RUN_ID% --repo %REPO% --json conclusion --jq ".conclusion"') do set CONCLUSION=%%i
    
    if "!CONCLUSION!"=="success" (
        echo ✅ 构建成功！
        goto download
    ) else (
        echo ❌ 构建失败: !CONCLUSION!
        gh run view %RUN_ID% --repo %REPO% --web
        exit /b 1
    )
)

echo ⏳ 构建中... (状态: %STATUS%)
timeout /t 10 /nobreak >nul
goto wait_loop

:download
echo.
echo 📥 下载APK...

REM 创建下载目录
if not exist %DOWNLOAD_DIR% mkdir %DOWNLOAD_DIR%
cd %DOWNLOAD_DIR%

REM 下载artifacts
gh run download %RUN_ID% --repo %REPO% --name app-debug

if not exist %APK_NAME% (
    echo ❌ APK文件不存在
    exit /b 1
)

set APK_PATH=%CD%\%APK_NAME%
echo ✅ APK已下载: %APK_PATH%
cd ..
echo.

REM 安装APK
echo 📲 安装APK到设备...
echo 🗑️  卸载旧版本...
adb uninstall com.douyin.automation 2>nul

echo 📦 安装新版本...
adb install -r "%APK_PATH%"

if %errorlevel% equ 0 (
    echo ✅ 安装成功！
    echo.
    echo 🚀 启动应用...
    adb shell am start -n com.douyin.automation/.MainActivity
    echo.
    echo ========================================
    echo ✅ 全部完成！
    echo APK位置: %APK_PATH%
    echo ========================================
) else (
    echo ❌ 安装失败
    exit /b 1
)

endlocal
