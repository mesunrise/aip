#!/usr/bin/env python3
"""
v0.1功能测试脚本
测试三个新增功能：
1. 连接成功时服务器返回"连接成功"
2. 发送消息时服务器回显收到的消息
3. 连接5秒后服务器主动推送"连接5秒"
"""
import asyncio
import websockets
import json
from datetime import datetime

SERVER_URL = "ws://localhost:50002/ws"

async def test_v01_features():
    """测试v0.1新增功能"""
    print("=" * 60)
    print("v0.1功能测试")
    print("=" * 60)
    print()
    
    try:
        print(f"🔗 连接服务器: {SERVER_URL}")
        async with websockets.connect(SERVER_URL) as websocket:
            
            # 测试1: 注册并接收"连接成功"消息
            print("\n【测试1】连接成功消息")
            print("-" * 60)
            register_msg = {
                "type": "register",
                "device_id": "test_v01_001"
            }
            await websocket.send(json.dumps(register_msg))
            print(f"📤 发送注册: {register_msg}")
            
            response = await websocket.recv()
            data = json.loads(response)
            print(f"📥 收到响应: {response}")
            
            if data.get("type") == "connected" and data.get("message") == "连接成功":
                print("✅ 测试1通过: 收到'连接成功'消息")
            else:
                print("❌ 测试1失败: 未收到正确的连接成功消息")
            
            # 测试2: 发送消息并验证服务器回显
            print("\n【测试2】消息回显")
            print("-" * 60)
            test_message = "Hello from v0.1 test!"
            msg = {
                "type": "message",
                "content": test_message
            }
            await websocket.send(json.dumps(msg))
            print(f"📤 发送消息: {test_message}")
            
            response = await websocket.recv()
            data = json.loads(response)
            print(f"📥 收到响应: {response}")
            
            if data.get("received_message") == test_message:
                print(f"✅ 测试2通过: 服务器正确回显消息 '{test_message}'")
            else:
                print(f"❌ 测试2失败: 服务器未正确回显消息")
            
            # 测试3: 等待5秒后的主动推送
            print("\n【测试3】服务器主动推送")
            print("-" * 60)
            print("⏳ 等待服务器主动推送（5秒后）...")
            
            # 设置超时为10秒，等待服务器推送
            try:
                response = await asyncio.wait_for(websocket.recv(), timeout=10)
                data = json.loads(response)
                print(f"📥 收到推送: {response}")
                
                if data.get("type") == "server_push" and data.get("message") == "连接5秒":
                    print("✅ 测试3通过: 收到服务器主动推送'连接5秒'")
                else:
                    print("❌ 测试3失败: 推送消息不正确")
            except asyncio.TimeoutError:
                print("❌ 测试3失败: 10秒内未收到服务器推送")
            
            print("\n" + "=" * 60)
            print("测试完成")
            print("=" * 60)
            
    except Exception as e:
        print(f"❌ 测试失败: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    print(f"⏰ 测试时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print()
    asyncio.run(test_v01_features())
