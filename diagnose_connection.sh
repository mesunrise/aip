#!/bin/bash

# WebSocket连接测试脚本集合

echo "=========================================="
echo "WebSocket连接诊断工具"
echo "=========================================="
echo ""

# 测试1: 检查服务器HTTP端点
echo "📋 测试1: 检查服务器HTTP端点"
echo "命令: curl http://elxn1431783.bohrium.tech:8086/"
echo "------------------------------------------"
curl -v http://elxn1431783.bohrium.tech:8086/ 2>&1 | head -20
echo ""
echo ""

# 测试2: 检查端口是否开放
echo "📋 测试2: 检查端口8086是否开放"
echo "命令: nc -zv elxn1431783.bohrium.tech 8086"
echo "------------------------------------------"
nc -zv elxn1431783.bohrium.tech 8086 2>&1 || echo "端口可能未开放或被防火墙阻止"
echo ""
echo ""

# 测试3: 使用websocat测试WebSocket
echo "📋 测试3: 使用Python测试WebSocket"
echo "命令: python3 test_websocket.py"
echo "------------------------------------------"
if command -v python3 &> /dev/null; then
    python3 test_websocket.py
else
    echo "❌ 未找到python3，跳过此测试"
fi
echo ""
echo ""

# 测试4: DNS解析
echo "📋 测试4: DNS解析测试"
echo "命令: nslookup elxn1431783.bohrium.tech"
echo "------------------------------------------"
nslookup elxn1431783.bohrium.tech 2>&1 || echo "DNS解析可能有问题"
echo ""
echo ""

# 测试5: Ping测试
echo "📋 测试5: Ping测试"
echo "命令: ping -c 3 elxn1431783.bohrium.tech"
echo "------------------------------------------"
ping -c 3 elxn1431783.bohrium.tech 2>&1 || echo "无法ping通服务器"
echo ""
echo ""

echo "=========================================="
echo "诊断完成"
echo "=========================================="
