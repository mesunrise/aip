#!/usr/bin/env python3
"""
测试8080端口WebSocket连接
"""
import asyncio
import websockets
import json
from datetime import datetime
import os

# 禁用代理
os.environ['NO_PROXY'] = '*'
os.environ['no_proxy'] = '*'

# 服务器地址
SERVER_URL = "ws://elxn1431783.bohrium.tech:8080/ws"

async def test_connection():
    """测试WebSocket连接"""
    print(f"🔗 正在连接到: {SERVER_URL}")
    print(f"⏰ 时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("-" * 50)
    
    try:
        async with websockets.connect(SERVER_URL) as websocket:
            print("✅ 外网连接成功!")
            
            # 1. 发送注册消息
            register_msg = {
                "type": "register",
                "device_id": "test_external_001"
            }
            await websocket.send(json.dumps(register_msg))
            print(f"📤 发送注册: {register_msg}")
            
            # 接收响应
            response = await websocket.recv()
            print(f"📥 收到响应: {response}")
            
            # 2. 发送测试消息
            test_msg = {
                "type": "message",
                "content": "Hello from external test!"
            }
            await websocket.send(json.dumps(test_msg))
            print(f"📤 发送消息: {test_msg}")
            
            # 接收响应
            response = await websocket.recv()
            print(f"📥 收到响应: {response}")
            
            print("-" * 50)
            print("✅ 外网测试通过! 8080端口可以访问!")
            print("")
            print("🎉 App可以使用以下地址连接:")
            print(f"   ws://elxn1431783.bohrium.tech:8080/ws")
            
    except Exception as e:
        print(f"❌ 连接失败: {e}")
        print("")
        print("⚠️  8080端口可能也被阻止")
        print("   建议联系管理员开放端口")

if __name__ == "__main__":
    print("=" * 50)
    print("外网WebSocket连接测试 (8080端口)")
    print("=" * 50)
    asyncio.run(test_connection())
