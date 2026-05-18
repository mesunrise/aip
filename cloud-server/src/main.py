import asyncio
import json
from datetime import datetime
from typing import Dict, Set
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.responses import HTMLResponse
import uvicorn
from src.task_scheduler import TaskScheduler

app = FastAPI(title="抖音自动化云端服务器")

# 存储所有连接的设备
connected_devices: Dict[str, WebSocket] = {}
device_info: Dict[str, dict] = {}

# 任务调度器
scheduler = TaskScheduler()

# 启动时加载任务
@app.on_event("startup")
async def startup_event():
    print("🚀 服务器启动中...")
    scheduler.load_tasks_from_md("tasks/automation-tasks.md")
    print("✅ 服务器启动完成")

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

@app.get("/api/tasks/stats")
async def get_task_stats():
    """获取任务统计"""
    return scheduler.get_stats()

@app.get("/api/tasks/{task_id}")
async def get_task_detail(task_id: str):
    """获取任务详情"""
    task = scheduler.get_task_detail(task_id)
    if task:
        return task
    return {"error": "Task not found"}

@app.post("/api/tasks/reload")
async def reload_tasks():
    """重新加载任务配置"""
    scheduler.load_tasks_from_md("tasks/automation-tasks.md")
    return {"message": "Tasks reloaded", "stats": scheduler.get_stats()}

@app.get("/test")
async def test_page():
    """浏览器测试页面"""
    html_content = """
    <!DOCTYPE html>
    <html>
    <head>
        <title>WebSocket测试页面</title>
        <meta charset="utf-8">
        <style>
            body {
                font-family: Arial, sans-serif;
                margin: 20px;
                background: #f5f5f5;
            }
            .container {
                max-width: 800px;
                margin: 0 auto;
                background: white;
                padding: 20px;
                border-radius: 10px;
                box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            }
            h1 { color: #333; }
            .status {
                padding: 10px;
                margin: 10px 0;
                border-radius: 5px;
                font-weight: bold;
            }
            .connected { background: #d4edda; color: #155724; }
            .disconnected { background: #f8d7da; color: #721c24; }
            input, button {
                padding: 10px;
                margin: 5px 0;
                font-size: 14px;
            }
            input { width: 100%; box-sizing: border-box; }
            button {
                cursor: pointer;
                background: #007bff;
                color: white;
                border: none;
                border-radius: 5px;
                padding: 10px 20px;
            }
            button:hover { background: #0056b3; }
            button:disabled {
                background: #ccc;
                cursor: not-allowed;
            }
            #log {
                background: #f8f9fa;
                padding: 10px;
                border: 1px solid #ddd;
                border-radius: 5px;
                height: 300px;
                overflow-y: auto;
                font-family: monospace;
                font-size: 12px;
            }
            .log-item {
                margin: 5px 0;
                padding: 5px;
                border-left: 3px solid #007bff;
                background: white;
            }
            .log-send { border-left-color: #28a745; }
            .log-receive { border-left-color: #17a2b8; }
            .log-error { border-left-color: #dc3545; }
        </style>
    </head>
    <body>
        <div class="container">
            <h1>🧪 WebSocket测试页面</h1>
            
            <div id="status" class="status disconnected">
                ● 未连接
            </div>
            
            <h3>服务器地址</h3>
            <input type="text" id="serverUrl" value="ws://localhost/ws" placeholder="ws://服务器地址/ws">
            
            <div style="margin: 10px 0;">
                <button id="connectBtn" onclick="connect()">连接</button>
                <button id="disconnectBtn" onclick="disconnect()" disabled>断开</button>
            </div>
            
            <h3>发送消息</h3>
            <input type="text" id="message" placeholder="输入测试消息" onkeypress="if(event.key==='Enter')sendMessage()">
            <button onclick="sendMessage()" id="sendBtn" disabled>发送消息</button>
            
            <h3>日志</h3>
            <div id="log"></div>
            
            <div style="margin-top: 20px; padding: 10px; background: #e7f3ff; border-radius: 5px;">
                <h4>📱 App连接地址</h4>
                <code id="appUrl">ws://当前域名/ws</code>
                <button onclick="copyAppUrl()" style="margin-left: 10px; padding: 5px 10px;">复制</button>
            </div>
        </div>
        
        <script>
            let ws = null;
            let deviceId = 'browser_test_' + Date.now();
            
            // 更新App连接地址
            document.getElementById('appUrl').textContent =
                'ws://' + window.location.host + '/ws';
            
            function addLog(message, type = 'info') {
                const log = document.getElementById('log');
                const item = document.createElement('div');
                item.className = 'log-item log-' + type;
                const time = new Date().toLocaleTimeString();
                item.textContent = `[${time}] ${message}`;
                log.appendChild(item);
                log.scrollTop = log.scrollHeight;
            }
            
            function updateStatus(connected) {
                const status = document.getElementById('status');
                const connectBtn = document.getElementById('connectBtn');
                const disconnectBtn = document.getElementById('disconnectBtn');
                const sendBtn = document.getElementById('sendBtn');
                
                if (connected) {
                    status.className = 'status connected';
                    status.textContent = '● 已连接';
                    connectBtn.disabled = true;
                    disconnectBtn.disabled = false;
                    sendBtn.disabled = false;
                } else {
                    status.className = 'status disconnected';
                    status.textContent = '● 未连接';
                    connectBtn.disabled = false;
                    disconnectBtn.disabled = true;
                    sendBtn.disabled = true;
                }
            }
            
            function connect() {
                const url = document.getElementById('serverUrl').value;
                addLog('正在连接: ' + url);
                
                try {
                    ws = new WebSocket(url);
                    
                    ws.onopen = function() {
                        addLog('✅ 连接成功!', 'receive');
                        updateStatus(true);
                        
                        // 发送注册消息
                        const registerMsg = {
                            type: 'register',
                            device_id: deviceId
                        };
                        ws.send(JSON.stringify(registerMsg));
                        addLog('📤 发送注册: ' + JSON.stringify(registerMsg), 'send');
                    };
                    
                    ws.onmessage = function(event) {
                        addLog('📥 收到消息: ' + event.data, 'receive');
                    };
                    
                    ws.onerror = function(error) {
                        addLog('❌ 连接错误', 'error');
                        updateStatus(false);
                    };
                    
                    ws.onclose = function() {
                        addLog('🔌 连接已关闭', 'error');
                        updateStatus(false);
                    };
                    
                } catch (error) {
                    addLog('❌ 连接失败: ' + error.message, 'error');
                    updateStatus(false);
                }
            }
            
            function disconnect() {
                if (ws) {
                    ws.close();
                    ws = null;
                    addLog('断开连接');
                    updateStatus(false);
                }
            }
            
            function sendMessage() {
                const message = document.getElementById('message').value;
                if (!message) {
                    alert('请输入消息');
                    return;
                }
                
                if (!ws || ws.readyState !== WebSocket.OPEN) {
                    alert('未连接到服务器');
                    return;
                }
                
                const msg = {
                    type: 'message',
                    content: message
                };
                
                ws.send(JSON.stringify(msg));
                addLog('📤 发送: ' + message, 'send');
                document.getElementById('message').value = '';
            }
            
            function copyAppUrl() {
                const url = document.getElementById('appUrl').textContent;
                navigator.clipboard.writeText(url).then(() => {
                    alert('已复制: ' + url);
                });
            }
            
            // 页面加载时的提示
            addLog('🎉 测试页面已加载');
            addLog('💡 点击"连接"按钮开始测试');
        </script>
    </body>
    </html>
    """
    return HTMLResponse(content=html_content)

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
    print(f"🔌 WebSocket连接已接受，等待注册消息...")
    
    # 创建一个任务来处理5秒后的主动推送
    async def send_delayed_message():
        await asyncio.sleep(5)
        try:
            await websocket.send_text(json.dumps({
                "type": "server_push",
                "message": "连接5秒",
                "timestamp": datetime.now().isoformat()
            }))
            print(f"📤 服务器主动推送 [{device_id}]: 连接5秒")
        except Exception as e:
            print(f"⚠️ 推送失败: {e}")
    
    try:
        # 等待设备发送注册消息
        print(f"⏳ 等待接收消息...")
        data = await websocket.receive_text()
        print(f"📩 收到原始数据: {data}")
        message = json.loads(data)
        print(f"📦 解析后的消息: {message}")

        if message.get("type") != "register":
            await websocket.close(code=1008, reason="first message must be register")
            print("⚠️ 首条消息不是 register，连接已关闭")
            return

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

        # 启动5秒延迟推送任务
        asyncio.create_task(send_delayed_message())

        # 处理消息循环
        while True:
            data = await websocket.receive_text()
            print(f"📩 [{device_id}] 收到原始数据: {data}")
            message = json.loads(data)
            print(f"📦 [{device_id}] 解析后的消息: {message}")

            message_type = message.get("type")

            if message_type == "heartbeat":
                device_info[device_id]["last_heartbeat"] = datetime.now().isoformat()
                await websocket.send_text(json.dumps({
                    "type": "heartbeat_ack",
                    "timestamp": datetime.now().isoformat()
                }))
                print(f"💓 [{device_id}] 心跳响应")

            elif message_type == "message":
                content = message.get("content", "")
                print(f"📨 [{device_id}] 收到消息: {content}")

                response = json.dumps({
                    "type": "message_ack",
                    "content": f"服务器收到: {content}",
                    "received_message": content,
                    "timestamp": datetime.now().isoformat()
                })
                print(f"📤 [{device_id}] 发送响应: {response}")
                await websocket.send_text(response)
                print(f"✅ [{device_id}] 响应已发送")

            elif message_type == "start_task":
                task = scheduler.get_next_task()
                if task is None:
                    print(f"⚠️ [{device_id}] 当前没有待执行任务")
                    await websocket.send_text(json.dumps({
                        "type": "no_task",
                        "timestamp": datetime.now().isoformat()
                    }))
                else:
                    print(f"🚀 [{device_id}] 下发任务: {task.get('task_id')}")
                    await scheduler.start_task(task, websocket)

            elif message_type == "step_result":
                task_id = message.get("task_id", "")
                step_index = message.get("step_index", -1)
                success = message.get("success", False)
                result_message = message.get("message", "")
                print(f"📋 [{device_id}] 步骤结果: task_id={task_id}, step_index={step_index}, success={success}, message={result_message}")
                scheduler.handle_step_result(task_id, step_index, success, result_message)

            elif message_type == "task_result":
                task_id = message.get("task_id", "")
                success = message.get("success", False)
                result = message.get("result", {})
                print(f"🏁 [{device_id}] 任务结果: task_id={task_id}, success={success}")
                scheduler.handle_task_result(task_id, success, result)

            else:
                print(f"⚠️ [{device_id}] 未识别消息类型: {message_type}")

    except WebSocketDisconnect:
        print(f"❌ WebSocket连接已断开")
    except Exception as e:
        print(f"⚠️ 错误: {e}")
    finally:
        if device_id and connected_devices.get(device_id) is websocket:
            connected_devices.pop(device_id, None)
            device_info.pop(device_id, None)
            print(f"🧹 已清理设备连接: {device_id}")

if __name__ == "__main__":
    print("🚀 启动服务器...")
    print("📱 WebSocket: ws://0.0.0.0:50002/ws")
    print("🌐 控制台: http://0.0.0.0:50002")
    print("🧪 测试页面: http://0.0.0.0:50002/test")
    uvicorn.run(app, host="0.0.0.0", port=50002)
