#!/usr/bin/env python3
"""
测试新服务器WebSocket连接
"""
import asyncio
import websockets
import json
from datetime import datetime
import os

# 禁用代理
os.environ['NO_PROXY'] = '*'
os.environ['no_proxy'] = '*'

# 新服务器地址
SERVER_URL = "ws://wdzd1450232.bohrium.tech:8080/ws"

async def test_connection():
    """测试WebSocket连接"""
    print(f"🔗 正在连接到: {SERVER_URL}")
    print(f"⏰ 时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("-" * 50)
    
    try:
        async with websockets.connect(SERVER_URL) as websocket:
            print("✅ 连接成功!")
            
            # 1. 发送注册消息
            register_msg = {
                "type": "register",
                "device_id": "test_new_server_001"
            }
            await websocket.send(json.dumps(register_msg))
            print(f"📤 发送注册: {register_msg}")
            
            # 接收响应
            response = await websocket.recv()
            print(f"📥 收到响应: {response}")
            
            # 2. 发送测试消息
            test_msg = {
                "type": "message",
                "content": "Hello from new server test!"
            }
            await websocket.send(json.dumps(test_msg))
            print(f"📤 发送消息: {test_msg}")
            
            # 接收响应
            response = await websocket.recv()
            print(f"📥 收到响应: {response}")
            
            # 3. 发送心跳
            heartbeat_msg = {
                "type": "heartbeat"
            }
            await websocket.send(json.dumps(heartbeat_msg))
            print(f"📤 发送心跳: {heartbeat_msg}")
            
            # 接收响应
            response = await websocket.recv()
            print(f"📥 收到响应: {response}")
            
            print("-" * 50)
            print("✅ 所有测试通过!")
            print("")
            print("🎉 新服务器工作正常!")
            print(f"📱 App可以使用: ws://wdzd1450232.bohrium.tech:8080/ws")
            
    except Exception as e:
        print(f"❌ 连接失败: {e}")
        print("")
        print("⚠️  可能的原因:")
        print("   1. 防火墙阻止8080端口")
        print("   2. 服务器未启动")
        print("   3. 网络问题")

if __name__ == "__main__":
    print("=" * 50)
    print("新服务器WebSocket连接测试")
    print("=" * 50)
    asyncio.run(test_connection())
