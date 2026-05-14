"""
v0.1 验收测试客户端
模拟Android App的WebSocket通信行为
"""
import asyncio
import json
import time
import websockets


SERVER_URL = "ws://localhost:8000/ws/test_device_001"


async def test_connection():
    """测试1: 基础连接"""
    print("\n=== 测试1: 基础连接 ===")
    async with websockets.connect(SERVER_URL) as ws:
        print("✅ 连接成功")
        return True


async def test_send_message():
    """测试2: 发送消息"""
    print("\n=== 测试2: 消息发送 ===")
    async with websockets.connect(SERVER_URL) as ws:
        msg = json.dumps({"type": "text", "content": "Hello Server"})
        await ws.send(msg)
        print(f"📤 发送: {msg}")

        response = await asyncio.wait_for(ws.recv(), timeout=5)
        print(f"📨 收到: {response}")
        assert "echo" in response or "Hello" in response or "command" in response
        print("✅ 消息收发正常")
        return True


async def test_heartbeat():
    """测试3: 心跳机制"""
    print("\n=== 测试3: 心跳机制 ===")
    async with websockets.connect(SERVER_URL) as ws:
        hb = json.dumps({"type": "heartbeat", "timestamp": time.time()})
        await ws.send(hb)
        print(f"💓 发送心跳: {hb}")

        response = await asyncio.wait_for(ws.recv(), timeout=5)
        data = json.loads(response)
        print(f"📨 收到: {response}")
        assert data.get("type") == "heartbeat_ack"
        print("✅ 心跳响应正常")
        return True


async def test_status_report():
    """测试4: 状态上报"""
    print("\n=== 测试4: 状态上报 ===")
    async with websockets.connect(SERVER_URL) as ws:
        status = json.dumps({
            "type": "status",
            "data": {"battery": 85, "screen": "on", "app": "idle"}
        })
        await ws.send(status)
        print(f"📊 上报状态: {status}")

        response = await asyncio.wait_for(ws.recv(), timeout=5)
        data = json.loads(response)
        print(f"📨 收到: {response}")
        assert data.get("type") == "status_ack"
        print("✅ 状态上报正常")
        return True


async def test_device_list():
    """测试5: 设备列表API"""
    print("\n=== 测试5: 设备列表API ===")
    import urllib.request
    with urllib.request.urlopen("http://localhost:8000/devices") as resp:
        data = json.loads(resp.read())
        print(f"📱 在线设备: {data}")
        print("✅ 设备列表API正常")
        return True


async def run_all_tests():
    print("=" * 50)
    print("  抖音自动化 v0.1 验收测试")
    print("=" * 50)

    results = {}

    try:
        results["连接测试"] = await test_connection()
    except Exception as e:
        results["连接测试"] = False
        print(f"❌ 失败: {e}")

    try:
        results["消息发送"] = await test_send_message()
    except Exception as e:
        results["消息发送"] = False
        print(f"❌ 失败: {e}")

    try:
        results["心跳机制"] = await test_heartbeat()
    except Exception as e:
        results["心跳机制"] = False
        print(f"❌ 失败: {e}")

    try:
        results["状态上报"] = await test_status_report()
    except Exception as e:
        results["状态上报"] = False
        print(f"❌ 失败: {e}")

    try:
        results["设备列表API"] = await test_device_list()
    except Exception as e:
        results["设备列表API"] = False
        print(f"❌ 失败: {e}")

    # 汇总
    print("\n" + "=" * 50)
    print("  测试结果汇总")
    print("=" * 50)
    passed = sum(1 for v in results.values() if v)
    total = len(results)
    for name, result in results.items():
        icon = "✅" if result else "❌"
        print(f"  {icon} {name}")
    print(f"\n  通过: {passed}/{total}")
    if passed == total:
        print("\n🎉 v0.1 验收通过！可以进入 v0.2 开发！")
    else:
        print("\n⚠️  部分测试未通过，请检查服务器日志")


if __name__ == "__main__":
    asyncio.run(run_all_tests())
