#!/bin/bash

# 从GitHub Actions下载最新成功构建的APK
# 需要GitHub Personal Access Token

set -e

REPO="mesunrise/aip"
APK_DIR="apk-releases"
GITHUB_TOKEN="${GITHUB_TOKEN:-}"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "📥 从GitHub Actions下载APK"
echo "================================"

# 检查GitHub Token
if [ -z "$GITHUB_TOKEN" ]; then
    echo -e "${YELLOW}⚠️  未设置GITHUB_TOKEN环境变量${NC}"
    echo "请设置GitHub Personal Access Token:"
    echo "  export GITHUB_TOKEN=your_token_here"
    echo ""
    echo "或者手动下载："
    echo "  https://github.com/$REPO/actions"
    exit 1
fi

# 创建APK目录
mkdir -p "$APK_DIR"

echo "🔍 查找最新成功的构建..."

# 获取最新成功的workflow run
RUN_DATA=$(curl -s -H "Authorization: token $GITHUB_TOKEN" \
    "https://api.github.com/repos/$REPO/actions/runs?status=success&per_page=1")

RUN_ID=$(echo "$RUN_DATA" | python3 -c "import sys, json; print(json.load(sys.stdin)['workflow_runs'][0]['id'])" 2>/dev/null || echo "")

if [ -z "$RUN_ID" ]; then
    echo -e "${RED}❌ 未找到成功的构建${NC}"
    exit 1
fi

echo -e "${GREEN}✅ 找到构建: Run ID $RUN_ID${NC}"

# 获取artifacts
echo "📦 获取artifacts..."
ARTIFACTS_DATA=$(curl -s -H "Authorization: token $GITHUB_TOKEN" \
    "https://api.github.com/repos/$REPO/actions/runs/$RUN_ID/artifacts")

ARTIFACT_ID=$(echo "$ARTIFACTS_DATA" | python3 -c "
import sys, json
data = json.load(sys.stdin)
if data['artifacts']:
    print(data['artifacts'][0]['id'])
" 2>/dev/null || echo "")

if [ -z "$ARTIFACT_ID" ]; then
    echo -e "${RED}❌ 未找到artifacts${NC}"
    exit 1
fi

ARTIFACT_NAME=$(echo "$ARTIFACTS_DATA" | python3 -c "
import sys, json
data = json.load(sys.stdin)
if data['artifacts']:
    print(data['artifacts'][0]['name'])
" 2>/dev/null || echo "app-debug")

echo -e "${GREEN}✅ 找到artifact: $ARTIFACT_NAME (ID: $ARTIFACT_ID)${NC}"

# 下载artifact
echo "⬇️  下载artifact..."
DOWNLOAD_URL="https://api.github.com/repos/$REPO/actions/artifacts/$ARTIFACT_ID/zip"

curl -L -H "Authorization: token $GITHUB_TOKEN" \
    -o "$APK_DIR/app-debug.zip" \
    "$DOWNLOAD_URL"

echo -e "${GREEN}✅ 下载完成${NC}"

# 解压
echo "📦 解压APK..."
cd "$APK_DIR"
unzip -o app-debug.zip
rm app-debug.zip

# 重命名为latest.apk
if [ -f "app-debug.apk" ]; then
    cp app-debug.apk latest.apk
    echo -e "${GREEN}✅ APK已保存到: $APK_DIR/latest.apk${NC}"
    
    # 显示文件信息
    SIZE=$(ls -lh latest.apk | awk '{print $5}')
    echo ""
    echo "📊 文件信息:"
    echo "  路径: $APK_DIR/latest.apk"
    echo "  大小: $SIZE"
    echo ""
    echo "🎉 下载完成！"
else
    echo -e "${RED}❌ 未找到app-debug.apk${NC}"
    exit 1
fi
