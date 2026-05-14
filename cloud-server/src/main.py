"""
抖音自动化云端服务器 - v0.1
功能：WebSocket通信验证
"""
import uvicorn
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.responses import HTMLResponse
from datetime import datetime
import json

app = FastAPI(title="抖音自动化云端服务器", version="0.1")

# 设备连接管理
connected_devices = {}


@app.get("/")
async def root():
    """首页 - 显示连接的设备"""
    html = f"""
    <html>
        <head><title>抖音自动化控制台</title></head>
        <body>
            <h1>抖音自动化控制台 v0.1</h1>
            <h2>在线设备: {len(connected_devices)}</h2>
            <ul>
                {''.join([f'<li>{device_id} - {info["connected_at"]}</li>' 
                         for device_id, info in connected_devices.items()])}
            </ul>
            <hr>
            <h3>测试消息发送</h3>
            <input id="deviceId" placeholder="设备ID" />
            <input id="message" placeholder="消息内容" />
            <button onclick="sendMessage()">发送</button>
            <script>
                function sendMessage() {{
                    const deviceId = document.getElementById('deviceId').value;
                    const message = document.getElementById('message').value;
                    fetch('/send/' + deviceId + '?message=' + message, {{method: 'POST'}})
                        .then(r => r.json())
                        .then(d => alert(JSON.stringify(d)));
                }}
            </script>
        </body>
    </html>
    """
    return HTMLResponse(content=html)


@app.websocket("/ws/{device_id}")
async def websocket_endpoint(websocket: WebSocket, device_id: str):
    """WebSocket连接端点"""
    await websocket.accept()
    
    # 注册设备
    connected_devices[device_id] = {
        "websocket": websocket,
        "connected_at": datetime.now().isoformat()
    }
    print(f"✅ 设备连接: {device_id}")
    
    try:
        while True:
            # 接收消息
            data = await websocket.receive_text()
            message = json.loads(data)
            
            print(f"📨 收到消息 [{device_id}]: {message}")
            
            # 处理心跳
            if message.get("type") == "heartbeat":
                await websocket.send_text(json.dumps({
                    "type": "heartbeat_ack",
                    "timestamp": datetime.now().timestamp()
                }))
            
            # 处理状态上报
            elif message.get("type") == "status":
                print(f"📊 设备状态 [{device_id}]: {message.get('data')}")
                await websocket.send_text(json.dumps({
                    "type": "status_ack",
                    "message": "状态已收到"
                }))
            
            # 回显其他消息
            else:
                await websocket.send_text(json.dumps({
                    "type": "echo",
                    "original": message
                }))
                
    except WebSocketDisconnect:
        print(f"❌ 设备断开: {device_id}")
        del connected_devices[device_id]


@app.post("/send/{device_id}")
async def send_message(device_id: str, message: str):
    """向指定设备发送消息"""
    if device_id not in connected_devices:
        return {"error": "设备未连接"}
    
    websocket = connected_devices[device_id]["websocket"]
    await websocket.send_text(json.dumps({
        "type": "command",
        "message": message,
        "timestamp": datetime.now().timestamp()
    }))
    
    return {"success": True, "device_id": device_id, "message": message}


@app.get("/devices")
async def list_devices():
    """列出所有连接的设备"""
    return {
        "count": len(connected_devices),
        "devices": [
            {
                "device_id": device_id,
                "connected_at": info["connected_at"]
            }
            for device_id, info in connected_devices.items()
        ]
    }


if __name__ == "__main__":
    print("🚀 启动服务器...")
    print("📱 WebSocket: ws://localhost:8000/ws/{device_id}")
    print("🌐 控制台: http://localhost:8000")
    uvicorn.run(app, host="0.0.0.0", port=8000)
