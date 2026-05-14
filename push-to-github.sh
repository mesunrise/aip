#!/bin/bash

# 推送代码到GitHub脚本
# 使用方法：
# 1. 设置环境变量：export GITHUB_TOKEN="your_token"
# 2. 设置环境变量：export GITHUB_USER="your_username"
# 3. 运行：bash push-to-github.sh

set -e

echo "🚀 开始推送代码到GitHub..."

# 检查环境变量
if [ -z "$GITHUB_TOKEN" ]; then
    echo "❌ 错误：请设置GITHUB_TOKEN环境变量"
    echo "   export GITHUB_TOKEN=\"ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx\""
    exit 1
fi

if [ -z "$GITHUB_USER" ]; then
    echo "❌ 错误：请设置GITHUB_USER环境变量"
    echo "   export GITHUB_USER=\"your_username\""
    exit 1
fi

# 仓库名称
REPO_NAME="aip"

# 检查是否已初始化Git
if [ ! -d ".git" ]; then
    echo "📦 初始化Git仓库..."
    git init
    
    echo "👤 配置Git用户信息..."
    git config user.email "isunrise@foxmail.com"
    git config user.name "$GITHUB_USER"
fi

# 添加所有文件
echo "📝 添加文件..."
git add .

# 提交
echo "💾 提交更改..."
git commit -m "feat: v0.1 初始版本 - WebSocket通信验证" || echo "没有新的更改"

# 检查远程仓库
if git remote | grep -q "origin"; then
    echo "🔄 远程仓库已存在，更新URL..."
    git remote set-url origin "https://${GITHUB_TOKEN}@github.com/${GITHUB_USER}/${REPO_NAME}.git"
else
    echo "🔗 添加远程仓库..."
    git remote add origin "https://${GITHUB_TOKEN}@github.com/${GITHUB_USER}/${REPO_NAME}.git"
fi

# 设置主分支
echo "🌿 设置主分支..."
git branch -M main

# 推送
echo "⬆️  推送到GitHub..."
git push -u origin main

echo ""
echo "✅ 推送成功！"
echo ""
echo "📱 查看仓库："
echo "   https://github.com/${GITHUB_USER}/${REPO_NAME}"
echo ""
echo "🔨 查看构建状态："
echo "   https://github.com/${GITHUB_USER}/${REPO_NAME}/actions"
echo ""
echo "⏱️  构建大约需要10-15分钟"
echo "📦 构建完成后可以在Artifacts下载APK"
echo ""
