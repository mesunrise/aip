#!/bin/bash

# APK自动下载脚本（服务器端）
# 功能：定期检查GitHub Actions构建，下载最新APK到服务器

set -e

REPO="mesunrise/aip"
WORKFLOW_NAME="build-apk.yml"
APK_NAME="app-debug.apk"
DOWNLOAD_DIR="/personal/ai_workspace/aip/apk-releases"
LATEST_FILE="$DOWNLOAD_DIR/latest.apk"

echo "🔍 检查最新构建..."

# 创建下载目录
mkdir -p $DOWNLOAD_DIR

# 获取最新成功的构建
RUN_ID=$(gh run list --repo $REPO --workflow $WORKFLOW_NAME --status completed --limit 1 --json databaseId,conclusion --jq '.[] | select(.conclusion=="success") | .databaseId')

if [ -z "$RUN_ID" ]; then
    echo "❌ 未找到成功的构建"
    exit 1
fi

echo "✅ 找到构建 ID: $RUN_ID"

# 检查是否已下载
MARKER_FILE="$DOWNLOAD_DIR/.downloaded_$RUN_ID"
if [ -f "$MARKER_FILE" ]; then
    echo "ℹ️  此版本已下载"
    exit 0
fi

echo "📥 下载APK..."
cd $DOWNLOAD_DIR

# 下载artifacts
gh run download $RUN_ID --repo $REPO --name app-debug

if [ ! -f "$APK_NAME" ]; then
    echo "❌ APK文件不存在"
    exit 1
fi

# 重命名为带时间戳的文件
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
NEW_NAME="app-debug-$TIMESTAMP-$RUN_ID.apk"
mv $APK_NAME $NEW_NAME

# 创建latest链接
ln -sf $NEW_NAME latest.apk

# 标记已下载
touch $MARKER_FILE

echo "✅ APK已下载: $NEW_NAME"
echo "📍 最新版本: $LATEST_FILE -> $NEW_NAME"

# 清理旧版本（保留最近5个）
echo "🧹 清理旧版本..."
ls -t app-debug-*.apk | tail -n +6 | xargs -r rm -f
ls -t .downloaded_* | tail -n +6 | xargs -r rm -f

echo "✅ 完成！"
