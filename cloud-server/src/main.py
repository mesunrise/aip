import asyncio
import json
from datetime import datetime
from typing import Dict, Set
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.responses import HTMLResponse
import uvicorn

app = FastAPI(title="抖音自动化云端服务器")

# 存储所有连接的设备
connected_devices: Dict[str, WebSocket] = {}
device_info: Dict[str, dict] = {}

@app.get("/")
async def root():
    """根路径 - 显示服务器状态"""
    return {
        "service": "抖音自动化云端服务器",
        "version": "0.1.0",
        "status": "running",
        "connected_devices": len(connected_devices),
        "devices": list(device_info.keys())
    }

@app.get("/devices")
async def get_devices():
    """获取所有连接的设备列表"""
    return {
        "count": len(connected_devices),
        "devices": device_info
    }

@app.get("/console")
async def console():
    """Web控制台"""
    html_content = """
    <!DOCTYPE html>
    <html>
    <head>
        <title>抖音自动化控制台</title>
        <meta charset="utf-8">
        <style>
            body { font-family: Arial, sans-serif; margin: 20px; }
            h1 { color: #333; }
            .device { border: 1px solid #ddd; padding: 10px; margin: 10px 0; border-radius: 5px; }
            .device h3 { margin: 0 0 10px 0; color: #0066cc; }
            .status { color: #00aa00; font-weight: bold; }
            button { padding: 5px 15px; margin: 5px; cursor: pointer; }
        </style>
    </head>
    <body>
        <h1>🚀 抖音自动化控制台</h1>
        <div id="devices"></div>
        <script>
            async function loadDevices() {
                const response = await fetch('/devices');
                const data = await response.json();
                const devicesDiv = document.getElementById('devices');
                devicesDiv.innerHTML = '<h2>在线设备 (' + data.count + ')</h2>';
                for (const [deviceId, info] of Object.entries(data.devices)) {
                    devicesDiv.innerHTML += `
                        <div class="device">
                            <h3>📱 ${deviceId}</h3>
                            <p><span class="status">● 在线</span></p>
                            <p>连接时间: ${info.connected_at}</p>
                        </div>
                    `;
                }
            }
            loadDevices();
            setInterval(loadDevices, 5000);
        </script>
    </body>
    </html>
    """
    return HTMLResponse(content=html_content)

@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    """WebSocket连接端点"""
    device_id = None
    await websocket.accept()
    
    try:
        # 等待设备发送注册消息
        data = await websocket.receive_text()
        message = json.loads(data)
        
        if message.get("type") == "register":
            device_id = message.get("device_id", f"device_{len(connected_devices)}")
            connected_devices[device_id] = websocket
            device_info[device_id] = {
                "connected_at": datetime.now().isoformat(),
                "last_heartbeat": datetime.now().isoformat()
            }
            
            print(f"✅ 设备已连接: {device_id}")
            
            # 发送连接成功消息
            await websocket.send_text(json.dumps({
                "type": "connected",
                "device_id": device_id,
                "message": "连接成功"
            }))
            
            # 处理消息循环
            while True:
                data = await websocket.receive_text()
                message = json.loads(data)
                
                if message.get("type") == "heartbeat":
                    # 心跳响应
                    device_info[device_id]["last_heartbeat"] = datetime.now().isoformat()
                    await websocket.send_text(json.dumps({
                        "type": "heartbeat_ack",
                        "timestamp": datetime.now().isoformat()
                    }))
                    print(f"💓 心跳: {device_id}")
                    
                elif message.get("type") == "message":
                    # 处理普通消息
                    content = message.get("content", "")
                    print(f"📨 收到消息 [{device_id}]: {content}")
                    
                    # 回复消息
                    await websocket.send_text(json.dumps({
                        "type": "message",
                        "content": f"服务器收到: {content}",
                        "timestamp": datetime.now().isoformat()
                    }))
                    
                elif message.get("type") == "status":
                    # 状态上报
                    status = message.get("status", {})
                    device_info[device_id].update(status)
                    print(f"📊 状态更新 [{device_id}]: {status}")
                    
    except WebSocketDisconnect:
        if device_id:
            print(f"❌ 设备已断开: {device_id}")
            connected_devices.pop(device_id, None)
            device_info.pop(device_id, None)
    except Exception as e:
        print(f"⚠️ 错误: {e}")
        if device_id:
            connected_devices.pop(device_id, None)
            device_info.pop(device_id, None)

if __name__ == "__main__":
    print("🚀 启动服务器...")
    print("📱 WebSocket: ws://0.0.0.0:8086/ws")
    print("🌐 控制台: http://0.0.0.0:8086")
    uvicorn.run(app, host="0.0.0.0", port=8086)
