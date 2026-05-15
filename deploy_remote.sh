#!/bin/bash

# 远程服务器部署脚本
# 目标服务器: wdzd1450232.bohrium.tech

SERVER="root@wdzd1450232.bohrium.tech"
PASSWORD="DgTcFf6z9cv0uUFB"
REMOTE_DIR="/root/aip"

echo "=========================================="
echo "部署到远程服务器"
echo "服务器: $SERVER"
echo "=========================================="
echo ""

# 1. 打包项目文件
echo "📦 打包项目文件..."
tar -czf aip-deploy.tar.gz \
    cloud-server/ \
    test_*.py \
    diagnose_connection.sh \
    --exclude='*.pyc' \
    --exclude='__pycache__'

echo "✅ 打包完成"
echo ""

# 2. 上传到服务器
echo "📤 上传到服务器..."
sshpass -p "$PASSWORD" scp aip-deploy.tar.gz $SERVER:/root/

echo "✅ 上传完成"
echo ""

# 3. 在服务器上解压并启动
echo "🚀 在服务器上部署..."
sshpass -p "$PASSWORD" ssh $SERVER << 'ENDSSH'
    # 解压
    cd /root
    rm -rf aip
    mkdir -p aip
    tar -xzf aip-deploy.tar.gz -C aip/
    cd aip
    
    # 安装依赖
    echo "📦 安装Python依赖..."
    pip3 install fastapi uvicorn websockets -q
    
    # 启动服务器
    echo "🚀 启动服务器..."
    cd cloud-server
    nohup python3 src/main.py > server.log 2>&1 &
    
    echo "✅ 服务器已启动"
    echo "📱 WebSocket: ws://wdzd1450232.bohrium.tech:8080/ws"
    echo "🌐 控制台: http://wdzd1450232.bohrium.tech:8080"
    
    # 等待服务器启动
    sleep 3
    
    # 测试本地连接
    echo ""
    echo "🧪 测试本地连接..."
    curl -s http://127.0.0.1:8080/ || echo "❌ 本地连接失败"
ENDSSH

echo ""
echo "=========================================="
echo "部署完成"
echo "=========================================="
echo ""
echo "📱 App连接地址:"
echo "   ws://wdzd1450232.bohrium.tech:8080/ws"
echo ""
echo "🧪 测试外网连接:"
echo "   curl http://wdzd1450232.bohrium.tech:8080/"
echo ""
