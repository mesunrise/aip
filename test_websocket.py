#!/usr/bin/env python3
"""
WebSocket连接测试脚本
用于测试服务器是否正常工作
"""
import asyncio
import websockets
import json
from datetime import datetime

# 服务器地址
SERVER_URL = "ws://elxn1431783.bohrium.tech:8086/ws"

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
                "device_id": "test_device_001"
            }
            await websocket.send(json.dumps(register_msg))
            print(f"📤 发送注册: {register_msg}")
            
            # 接收响应
            response = await websocket.recv()
            print(f"📥 收到响应: {response}")
            
            # 2. 发送测试消息
            test_msg = {
                "type": "message",
                "content": "Hello from test script!"
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
            
    except websockets.exceptions.WebSocketException as e:
        print(f"❌ WebSocket错误: {e}")
    except ConnectionRefusedError:
        print(f"❌ 连接被拒绝: 服务器可能未运行或端口不正确")
    except TimeoutError:
        print(f"❌ 连接超时: 检查网络或服务器地址")
    except Exception as e:
        print(f"❌ 未知错误: {e}")

if __name__ == "__main__":
    print("=" * 50)
    print("WebSocket连接测试")
    print("=" * 50)
    asyncio.run(test_connection())
