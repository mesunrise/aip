"""
任务调度器
负责读取任务配置、管理任务队列、发送任务到App
"""
import asyncio
import json
import yaml
import re
from datetime import datetime
from typing import List, Dict, Optional
from pathlib import Path

class TaskScheduler:
    def __init__(self):
        self.tasks: List[Dict] = []
        self.running_tasks: Dict[str, Dict] = {}
        self.completed_tasks: List[Dict] = []
        self.failed_tasks: List[Dict] = []
        self.step_waiters: Dict[str, asyncio.Event] = {}
        self.task_waiters: Dict[str, asyncio.Event] = {}
        
    def load_tasks_from_md(self, filepath: str = "tasks/automation-tasks.md"):
        """从MD文档加载任务"""
        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # 使用正则表达式提取YAML代码块
            yaml_blocks = re.findall(r'```yaml\n(.*?)\n```', content, re.DOTALL)
            
            for yaml_str in yaml_blocks:
                try:
                    task = yaml.safe_load(yaml_str)
                    if task and isinstance(task, dict) and 'task_id' in task:
                        # 只加载pending状态的任务
                        if task.get('status') == 'pending':
                            self.tasks.append(task)
                            print(f"✅ 加载任务: {task.get('task_id')} - {task.get('task_name', 'N/A')}")
                except yaml.YAMLError as e:
                    print(f"⚠️ 解析YAML失败: {e}")
            
            # 按优先级排序
            self.tasks.sort(key=lambda x: x.get('priority', 999))
            print(f"📋 共加载 {len(self.tasks)} 个待执行任务")
            
        except FileNotFoundError:
            print(f"❌ 任务配置文件不存在: {filepath}")
        except Exception as e:
            print(f"❌ 加载任务失败: {e}")
    
    def add_task(self, task: Dict):
        """添加任务"""
        self.tasks.append(task)
        self.tasks.sort(key=lambda x: x.get('priority', 999))
        print(f"✅ 添加任务: {task.get('task_id')}")
    
    def get_next_task(self) -> Optional[Dict]:
        """获取下一个待执行任务"""
        for task in self.tasks:
            if task.get('status') == 'pending':
                return task
        return None
    
    async def start_task(self, task: Dict, websocket):
        """开始执行任务"""
        task_id = task['task_id']
        task_type = task['type']
        
        # 更新状态
        task['status'] = 'running'
        task['started_at'] = datetime.now().isoformat()
        self.running_tasks[task_id] = task
        
        print(f"🚀 开始执行任务: {task_id} - {task.get('task_name', 'N/A')}")
        
        # 根据任务类型发送不同的指令
        if task_type == 'search_and_explore':
            await self.execute_search_and_explore(task, websocket)
        elif task_type == 'search_blogger':
            await self.execute_search_blogger(task, websocket)
        else:
            print(f"⚠️ 未知任务类型: {task_type}")
    
    async def execute_search_and_explore(self, task: Dict, websocket):
        """执行搜索并探索任务"""
        task_id = task['task_id']
        config = task.get('config', {})
        steps = config.get('steps', [])
        
        print(f"📝 任务包含 {len(steps)} 个步骤")
        
        # 发送任务开始消息
        await websocket.send_json({
            "type": "task_start",
            "task_id": task_id,
            "task_name": task.get('task_name'),
            "total_steps": len(steps)
        })
        
        # 逐步执行
        for index, step in enumerate(steps, 1):
            action = step.get('action')
            print(f"📍 步骤 {index}/{len(steps)}: {action}")

            step_event = asyncio.Event()
            self.step_waiters[f"{task_id}:{index}"] = step_event

            # 发送步骤指令
            message = {
                "type": "step",
                "task_id": task_id,
                "step_index": index,
                "action": action,
                **step  # 包含所有步骤参数
            }

            await websocket.send_json(message)

            try:
                await asyncio.wait_for(step_event.wait(), timeout=30)
            except asyncio.TimeoutError:
                print(f"⏰ 步骤执行超时: {task_id} step {index}")
                self.handle_task_result(task_id, False, {
                    "error": f"step {index} timeout",
                    "step_index": index,
                    "action": action
                })
                return
            finally:
                self.step_waiters.pop(f"{task_id}:{index}", None)

            step_results = task.get('step_results', [])
            latest_result = step_results[-1] if step_results else None
            if latest_result and not latest_result.get('success', False):
                print(f"❌ 步骤失败，终止任务: {task_id} step {index}")
                self.handle_task_result(task_id, False, {
                    "error": latest_result.get('message', 'step failed'),
                    "step_index": index,
                    "action": action
                })
                return
    
    async def execute_search_blogger(self, task: Dict, websocket):
        """执行搜索博主任务"""
        task_id = task['task_id']
        config = task.get('config', {})
        blogger_name = config.get('blogger_name')
        
        print(f"🔍 搜索博主: {blogger_name}")
        
        # 发送搜索指令
        await websocket.send_json({
            "type": "search_blogger",
            "task_id": task_id,
            "blogger_name": blogger_name
        })
    
    def handle_step_result(self, task_id: str, step_index: int, success: bool, message: str = ""):
        """处理步骤执行结果"""
        if task_id not in self.running_tasks:
            print(f"⚠️ 任务不存在: {task_id}")
            return
        
        task = self.running_tasks[task_id]
        
        if 'step_results' not in task:
            task['step_results'] = []
        
        task['step_results'].append({
            'step_index': step_index,
            'success': success,
            'message': message,
            'timestamp': datetime.now().isoformat()
        })
        
        waiter = self.step_waiters.get(f"{task_id}:{step_index}")
        if waiter:
            waiter.set()

        status = "✅" if success else "❌"
        print(f"{status} 步骤 {step_index}: {message}")
    
    def handle_task_result(self, task_id: str, success: bool, result: Dict = None):
        """处理任务完成结果"""
        if task_id not in self.running_tasks:
            print(f"⚠️ 任务不存在: {task_id}")
            return
        
        task = self.running_tasks[task_id]
        task['status'] = 'completed' if success else 'failed'
        task['completed_at'] = datetime.now().isoformat()
        task['result'] = result or {}

        task_waiter = self.task_waiters.get(task_id)
        if task_waiter:
            task_waiter.set()

        # 从运行队列移除
        del self.running_tasks[task_id]
        
        # 添加到完成/失败列表
        if success:
            self.completed_tasks.append(task)
            print(f"✅ 任务完成: {task_id}")
        else:
            self.failed_tasks.append(task)
            print(f"❌ 任务失败: {task_id}")
            
            # 检查是否需要重试
            retry_count = task.get('config', {}).get('retry_count', 0)
            current_retry = task.get('retry_attempt', 0)
            
            if current_retry < retry_count:
                print(f"🔄 准备重试 ({current_retry + 1}/{retry_count})")
                task['status'] = 'pending'
                task['retry_attempt'] = current_retry + 1
                self.tasks.append(task)
        
        # 从待执行队列移除
        self.tasks = [t for t in self.tasks if t['task_id'] != task_id]
        
        # 记录日志
        self.log_task_result(task)
    
    def log_task_result(self, task: Dict):
        """记录任务结果到日志文件"""
        task_id = task['task_id']
        log_dir = Path("tasks/logs")
        log_dir.mkdir(parents=True, exist_ok=True)
        
        log_file = log_dir / f"{task_id}.log"
        
        with open(log_file, 'w', encoding='utf-8') as f:
            f.write(f"任务ID: {task_id}\n")
            f.write(f"任务名称: {task.get('task_name', 'N/A')}\n")
            f.write(f"任务类型: {task['type']}\n")
            f.write(f"状态: {task['status']}\n")
            f.write(f"开始时间: {task.get('started_at', 'N/A')}\n")
            f.write(f"完成时间: {task.get('completed_at', 'N/A')}\n")
            f.write(f"\n步骤执行记录:\n")
            
            for step_result in task.get('step_results', []):
                status = "✅" if step_result['success'] else "❌"
                f.write(f"{status} 步骤 {step_result['step_index']}: {step_result['message']}\n")
            
            f.write(f"\n最终结果:\n")
            f.write(json.dumps(task.get('result', {}), indent=2, ensure_ascii=False))
        
        print(f"📝 日志已保存: {log_file}")
    
    def get_stats(self) -> Dict:
        """获取任务统计"""
        return {
            "total": len(self.tasks) + len(self.running_tasks) + len(self.completed_tasks) + len(self.failed_tasks),
            "pending": len([t for t in self.tasks if t.get('status') == 'pending']),
            "running": len(self.running_tasks),
            "completed": len(self.completed_tasks),
            "failed": len(self.failed_tasks)
        }
    
    def get_task_detail(self, task_id: str) -> Optional[Dict]:
        """获取任务详情"""
        # 在所有队列中查找
        for task in self.tasks + list(self.running_tasks.values()) + self.completed_tasks + self.failed_tasks:
            if task.get('task_id') == task_id:
                return task
        return None
