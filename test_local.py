#!/usr/bin/env python3
"""
本地WebSocket连接测试（绕过代理）
"""
import asyncio
import websockets
import json
from datetime import datetime
import os

# 禁用代理
os.environ['NO_PROXY'] = '*'
os.environ['no_proxy'] = '*'

# 本地服务器地址
SERVER_URL = "ws://127.0.0.1:8086/ws"

async def test_local_connection():
    """测试本地WebSocket连接"""
    print(f"🔗 正在连接到本地服务器: {SERVER_URL}")
    print(f"⏰ 时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("-" * 50)
    
    try:
        async with websockets.connect(SERVER_URL) as websocket:
            print("✅ 本地连接成功!")
            
            # 1. 发送注册消息
            register_msg = {
                "type": "register",
                "device_id": "test_local_001"
            }
            await websocket.send(json.dumps(register_msg))
            print(f"📤 发送注册: {register_msg}")
            
            # 接收响应
            response = await websocket.recv()
            print(f"📥 收到响应: {response}")
            
            # 2. 发送测试消息
            test_msg = {
                "type": "message",
                "content": "Hello from local test!"
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
            print("✅ 本地测试通过! 服务器工作正常!")
            print("")
            print("⚠️  问题诊断:")
            print("   - 本地连接成功 ✅")
            print("   - 说明服务器代码正常")
            print("   - 外网连接失败可能是:")
            print("     1. 防火墙阻止8086端口")
            print("     2. 域名解析问题")
            print("     3. 网络代理问题")
            
    except Exception as e:
        print(f"❌ 连接失败: {e}")
        print("")
        print("⚠️  如果本地也连接失败:")
        print("   - 检查服务器是否运行")
        print("   - 检查端口8086是否被占用")

if __name__ == "__main__":
    print("=" * 50)
    print("本地WebSocket连接测试")
    print("=" * 50)
    asyncio.run(test_local_connection())
