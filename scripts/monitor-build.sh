#!/bin/bash

# GitHub Actions构建监控脚本
# 功能：实时监控最新构建状态

REPO="mesunrise/aip"
CHECK_INTERVAL=30  # 检查间隔（秒）

echo "🔍 开始监控GitHub Actions构建..."
echo "📦 仓库: $REPO"
echo "⏱️  检查间隔: ${CHECK_INTERVAL}秒"
echo ""

while true; do
    # 获取最新构建信息
    RESPONSE=$(curl -s "https://api.github.com/repos/$REPO/actions/runs?per_page=1")
    
    if [ $? -ne 0 ]; then
        echo "❌ API请求失败"
        sleep $CHECK_INTERVAL
        continue
    fi
    
    # 解析JSON
    RUN_ID=$(echo "$RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['workflow_runs'][0]['id'])" 2>/dev/null)
    STATUS=$(echo "$RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['workflow_runs'][0]['status'])" 2>/dev/null)
    CONCLUSION=$(echo "$RESPONSE" | python3 -c "import sys, json; run=json.load(sys.stdin)['workflow_runs'][0]; print(run.get('conclusion', 'N/A'))" 2>/dev/null)
    CREATED=$(echo "$RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['workflow_runs'][0]['created_at'])" 2>/dev/null)
    URL=$(echo "$RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['workflow_runs'][0]['html_url'])" 2>/dev/null)
    
    # 显示状态
    TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')
    echo "[$TIMESTAMP]"
    echo "  Run ID: $RUN_ID"
    echo "  Status: $STATUS"
    echo "  Conclusion: $CONCLUSION"
    echo "  Created: $CREATED"
    echo "  URL: $URL"
    
    # 检查是否完成
    if [ "$STATUS" = "completed" ]; then
        echo ""
        if [ "$CONCLUSION" = "success" ]; then
            echo "✅ 构建成功！"
            echo "📥 下载APK："
            echo "   1. 访问: $URL"
            echo "   2. 滚动到底部 'Artifacts'"
            echo "   3. 下载 'app-debug'"
            exit 0
        elif [ "$CONCLUSION" = "failure" ]; then
            echo "❌ 构建失败！"
            echo "🔍 查看日志: $URL"
            exit 1
        else
            echo "⚠️  构建完成但状态异常: $CONCLUSION"
            exit 2
        fi
    fi
    
    echo "  ⏳ 构建进行中..."
    echo ""
    
    # 等待下一次检查
    sleep $CHECK_INTERVAL
done
