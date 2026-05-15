#!/bin/bash

# APK自动下载和安装脚本
# 功能：监控GitHub Actions构建，完成后自动下载APK并安装到手机

set -e

# 配置
REPO="mesunrise/aip"
WORKFLOW_NAME="build-apk.yml"
APK_NAME="app-debug.apk"
DOWNLOAD_DIR="./apk-downloads"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 APK自动下载和安装脚本${NC}"
echo "================================"

# 检查依赖
check_dependencies() {
    echo "📋 检查依赖..."
    
    if ! command -v gh &> /dev/null; then
        echo -e "${RED}❌ 未安装 GitHub CLI (gh)${NC}"
        echo "请安装: https://cli.github.com/"
        exit 1
    fi
    
    if ! command -v adb &> /dev/null; then
        echo -e "${RED}❌ 未安装 ADB${NC}"
        echo "请安装 Android SDK Platform Tools"
        exit 1
    fi
    
    echo -e "${GREEN}✅ 依赖检查通过${NC}"
}

# 检查设备连接
check_device() {
    echo "📱 检查设备连接..."
    
    DEVICES=$(adb devices | grep -v "List" | grep "device$" | wc -l)
    
    if [ "$DEVICES" -eq 0 ]; then
        echo -e "${RED}❌ 未检测到Android设备${NC}"
        echo "请确保："
        echo "  1. 手机已通过USB连接"
        echo "  2. 已开启USB调试"
        echo "  3. 已授权此电脑"
        exit 1
    fi
    
    echo -e "${GREEN}✅ 检测到 $DEVICES 个设备${NC}"
    adb devices
}

# 获取最新构建
get_latest_run() {
    echo "🔍 获取最新构建..."
    
    RUN_ID=$(gh run list --repo $REPO --workflow $WORKFLOW_NAME --limit 1 --json databaseId --jq '.[0].databaseId')
    
    if [ -z "$RUN_ID" ]; then
        echo -e "${RED}❌ 未找到构建记录${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ 找到构建 ID: $RUN_ID${NC}"
    echo "$RUN_ID"
}

# 等待构建完成
wait_for_build() {
    local RUN_ID=$1
    echo "⏳ 等待构建完成..."
    
    while true; do
        STATUS=$(gh run view $RUN_ID --repo $REPO --json status --jq '.status')
        CONCLUSION=$(gh run view $RUN_ID --repo $REPO --json conclusion --jq '.conclusion')
        
        if [ "$STATUS" = "completed" ]; then
            if [ "$CONCLUSION" = "success" ]; then
                echo -e "${GREEN}✅ 构建成功！${NC}"
                return 0
            else
                echo -e "${RED}❌ 构建失败: $CONCLUSION${NC}"
                gh run view $RUN_ID --repo $REPO --web
                exit 1
            fi
        fi
        
        echo "⏳ 构建中... (状态: $STATUS)"
        sleep 10
    done
}

# 下载APK
download_apk() {
    local RUN_ID=$1
    echo "📥 下载APK..."
    
    # 创建下载目录
    mkdir -p $DOWNLOAD_DIR
    cd $DOWNLOAD_DIR
    
    # 下载artifacts
    gh run download $RUN_ID --repo $REPO --name app-debug
    
    if [ ! -f "$APK_NAME" ]; then
        echo -e "${RED}❌ APK文件不存在${NC}"
        exit 1
    fi
    
    APK_PATH=$(pwd)/$APK_NAME
    echo -e "${GREEN}✅ APK已下载: $APK_PATH${NC}"
    cd ..
    echo "$APK_PATH"
}

# 安装APK
install_apk() {
    local APK_PATH=$1
    echo "📲 安装APK到设备..."
    
    # 卸载旧版本（如果存在）
    echo "🗑️  卸载旧版本..."
    adb uninstall com.douyin.automation 2>/dev/null || true
    
    # 安装新版本
    echo "📦 安装新版本..."
    adb install -r "$APK_PATH"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 安装成功！${NC}"
        
        # 启动应用
        echo "🚀 启动应用..."
        adb shell am start -n com.douyin.automation/.MainActivity
        
        echo -e "${GREEN}🎉 完成！应用已启动${NC}"
    else
        echo -e "${RED}❌ 安装失败${NC}"
        exit 1
    fi
}

# 主流程
main() {
    check_dependencies
    check_device
    
    RUN_ID=$(get_latest_run)
    wait_for_build $RUN_ID
    APK_PATH=$(download_apk $RUN_ID)
    install_apk "$APK_PATH"
    
    echo ""
    echo "================================"
    echo -e "${GREEN}✅ 全部完成！${NC}"
    echo "APK位置: $APK_PATH"
    echo "================================"
}

# 运行
main
