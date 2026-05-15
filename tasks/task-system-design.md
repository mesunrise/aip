# 任务管理系统设计

## 📋 系统概述

任务管理系统负责管理和调度抖音自动化任务，支持任务的创建、执行、监控和结果收集。

## 🏗️ 系统架构

```
┌─────────────────┐
│  任务配置文件    │
│ (tasks.md)      │
└────────┬────────┘
         │
         ↓
┌─────────────────┐      WebSocket      ┌─────────────────┐
│   任务调度器     │ ←─────────────────→ │   Android App   │
│  (Scheduler)    │                     │                 │
└────────┬────────┘                     └─────────────────┘
         │
         ↓
┌─────────────────┐
│   任务数据库     │
│  (SQLite/PG)    │
└─────────────────┘
```

## 📁 文件结构

```
tasks/
├── automation-tasks.md          # 任务配置文档
├── task-templates/              # 任务模板
│   ├── search_blogger.yaml
│   ├── collect_comments.yaml
│   └── send_message.yaml
└── logs/                        # 执行日志
    ├── 2026-05-15.log
    └── task_001.log
```

## 🔧 服务器端实现

### 1. 任务调度器

**文件：** `cloud-server/src/task_scheduler.py`

```python
import asyncio
from typing import List, Dict
from datetime import datetime

class TaskScheduler:
    def __init__(self):
        self.tasks: List[Dict] = []
        self.running_tasks: Dict = {}
        
    def load_tasks_from_md(self, filepath: str):
        """从MD文档加载任务"""
        # 解析MD文档，提取任务配置
        pass
    
    def add_task(self, task: Dict):
        """添加任务"""
        self.tasks.append(task)
        self.tasks.sort(key=lambda x: x['priority'])
    
    async def execute_next_task(self, websocket):
        """执行下一个任务"""
        if not self.tasks:
            return
        
        task = self.tasks[0]
        if task['status'] != 'pending':
            return
        
        # 更新状态
        task['status'] = 'running'
        task['started_at'] = datetime.now().isoformat()
        
        # 发送到App
        await websocket.send_json({
            "type": task['type'],
            "task_id": task['task_id'],
            **task['config']
        })
        
        self.running_tasks[task['task_id']] = task
    
    def handle_task_result(self, task_id: str, result: Dict):
        """处理任务结果"""
        if task_id not in self.running_tasks:
            return
        
        task = self.running_tasks[task_id]
        task['status'] = 'completed' if result['success'] else 'failed'
        task['completed_at'] = datetime.now().isoformat()
        task['result'] = result
        
        # 从运行队列移除
        del self.running_tasks[task_id]
        
        # 从待执行队列移除
        self.tasks = [t for t in self.tasks if t['task_id'] != task_id]
        
        # 记录日志
        self.log_task_result(task)
    
    def log_task_result(self, task: Dict):
        """记录任务结果"""
        log_file = f"tasks/logs/{task['task_id']}.log"
        with open(log_file, 'w') as f:
            f.write(f"Task ID: {task['task_id']}\n")
            f.write(f"Type: {task['type']}\n")
            f.write(f"Status: {task['status']}\n")
            f.write(f"Started: {task.get('started_at')}\n")
            f.write(f"Completed: {task.get('completed_at')}\n")
            f.write(f"Result: {task.get('result')}\n")
```

### 2. 集成到主服务器

**文件：** `cloud-server/src/main.py`

```python
from task_scheduler import TaskScheduler

# 初始化调度器
scheduler = TaskScheduler()

# 加载任务
scheduler.load_tasks_from_md("tasks/automation-tasks.md")

@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    
    try:
        # 连接成功后，开始执行任务
        await scheduler.execute_next_task(websocket)
        
        while True:
            data = await websocket.receive_text()
            message = json.loads(data)
            
            # 处理任务结果
            if message.get("type") == "search_result":
                task_id = message.get("task_id")
                scheduler.handle_task_result(task_id, message)
                
                # 执行下一个任务
                await scheduler.execute_next_task(websocket)
            
    except WebSocketDisconnect:
        print("Client disconnected")
```

## 📊 任务状态管理

### 状态转换图

```
pending → running → completed
                 ↘ failed → pending (retry)
```

### 状态更新时机

1. **pending → running**
   - 任务被发送到App时

2. **running → completed**
   - App上报成功结果时

3. **running → failed**
   - App上报失败结果时
   - 任务超时时

4. **failed → pending**
   - 自动重试时

## 🔍 任务监控

### 监控指标

1. **任务执行统计**
   - 总任务数
   - 成功任务数
   - 失败任务数
   - 平均执行时间

2. **实时状态**
   - 待执行任务数
   - 正在执行任务数
   - 任务队列长度

3. **性能指标**
   - 任务吞吐量
   - 成功率
   - 平均响应时间

### 监控接口

```python
@app.get("/api/tasks/stats")
async def get_task_stats():
    return {
        "total": len(scheduler.tasks),
        "pending": len([t for t in scheduler.tasks if t['status'] == 'pending']),
        "running": len(scheduler.running_tasks),
        "completed": 0,  # 从数据库查询
        "failed": 0      # 从数据库查询
    }

@app.get("/api/tasks/{task_id}")
async def get_task_detail(task_id: str):
    # 返回任务详情
    pass
```

## 📝 使用示例

### 1. 添加新任务

**方式1：编辑MD文档**
```markdown
### 任务 #4: 搜索博主 - 新博主
\`\`\`yaml
task_id: task_004
type: search_blogger
status: pending
priority: 4
config:
  blogger_name: "新博主"
  retry_count: 3
  timeout: 60
created_at: "2026-05-15 13:00:00"
\`\`\`
```

**方式2：通过API**
```bash
curl -X POST http://localhost:8000/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "type": "search_blogger",
    "config": {
      "blogger_name": "新博主"
    }
  }'
```

### 2. 查询任务状态

```bash
curl http://localhost:8000/api/tasks/task_001
```

### 3. 取消任务

```bash
curl -X DELETE http://localhost:8000/api/tasks/task_001
```

## 🎯 最佳实践

### 1. 任务优先级设置
- 紧急任务：priority = 1
- 普通任务：priority = 5
- 低优先级：priority = 10

### 2. 超时时间设置
- 搜索博主：60秒
- 采集评论：300秒
- 发送私信：30秒

### 3. 重试策略
- 网络错误：立即重试
- 元素未找到：延迟30秒重试
- 其他错误：延迟60秒重试

### 4. 限流控制
- 每小时最多10个搜索任务
- 任务间隔至少30秒
- 失败后延迟60秒再试

---

**文档版本：** 1.0  
**创建时间：** 2026-05-15  
**维护者：** 开发团队
