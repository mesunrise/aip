#!/bin/bash

# 自动监控GitHub Actions并下载最新成功的APK
# 每5分钟检查一次

set -e

REPO="mesunrise/aip"
APK_DIR="apk-releases"
CHECK_INTERVAL=300  # 5分钟
LAST_RUN_ID=""

echo "🤖 启动APK自动下载监控"
echo "================================"
echo "仓库: $REPO"
echo "检查间隔: ${CHECK_INTERVAL}秒"
echo "APK目录: $APK_DIR"
echo ""

while true; do
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] 🔍 检查新构建..."
    
    # 获取最新成功的run ID
    CURRENT_RUN_ID=$(curl -s "https://api.github.com/repos/$REPO/actions/runs?status=success&per_page=1" | \
        python3 -c "import sys, json; print(json.load(sys.stdin)['workflow_runs'][0]['id'])" 2>/dev/null || echo "")
    
    if [ -z "$CURRENT_RUN_ID" ]; then
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ⚠️  未找到成功的构建"
    elif [ "$CURRENT_RUN_ID" != "$LAST_RUN_ID" ]; then
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] 🎉 发现新构建: Run ID $CURRENT_RUN_ID"
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] 📥 开始下载APK..."
        
        # 调用下载脚本
        if python3 scripts/download-apk.py; then
            LAST_RUN_ID="$CURRENT_RUN_ID"
            echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✅ APK下载成功"
        else
            echo "[$(date '+%Y-%m-%d %H:%M:%S')] ❌ APK下载失败"
        fi
    else
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ℹ️  没有新构建"
    fi
    
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ⏰ 等待${CHECK_INTERVAL}秒..."
    echo ""
    sleep $CHECK_INTERVAL
done
