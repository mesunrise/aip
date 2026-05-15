# 抖音自动化任务配置

## 📋 任务配置说明

本文档用于配置抖音自动化任务。服务器会读取此配置并按顺序执行任务。

## 🎯 任务类型

### 1. 搜索博主任务 (search_blogger)
搜索指定博主并进入主页

### 2. 采集评论任务 (collect_comments)
采集指定作品的评论（v0.3实现）

### 3. 发送私信任务 (send_message)
向指定用户发送私信（v0.4实现）

---

## 📝 当前任务列表

### 任务 #1: 搜索博主 - 罗翔说刑法
```yaml
task_id: task_001
type: search_blogger
status: pending
priority: 1
config:
  blogger_name: "罗翔说刑法"
  retry_count: 3
  timeout: 60
created_at: "2026-05-15 12:00:00"
```

**任务说明：**
- 搜索博主"罗翔说刑法"
- 进入博主主页
- 验证是否成功进入
- 上报执行结果

---

### 任务 #2: 搜索博主 - 李永乐老师
```yaml
task_id: task_002
type: search_blogger
status: pending
priority: 2
config:
  blogger_name: "李永乐老师"
  retry_count: 3
  timeout: 60
created_at: "2026-05-15 12:05:00"
```

**任务说明：**
- 搜索博主"李永乐老师"
- 进入博主主页
- 验证是否成功进入
- 上报执行结果

---

### 任务 #3: 搜索博主 - 半佛仙人
```yaml
task_id: task_003
type: search_blogger
status: pending
priority: 3
config:
  blogger_name: "半佛仙人"
  retry_count: 3
  timeout: 60
created_at: "2026-05-15 12:10:00"
```

**任务说明：**
- 搜索博主"半佛仙人"
- 进入博主主页
- 验证是否成功进入
- 上报执行结果

---

## 🔧 任务配置参数说明

### 通用参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| task_id | string | ✅ | 任务唯一标识 |
| type | string | ✅ | 任务类型 |
| status | string | ✅ | 任务状态：pending/running/completed/failed |
| priority | int | ✅ | 优先级（数字越小优先级越高） |
| created_at | string | ✅ | 创建时间 |

### search_blogger 参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| blogger_name | string | ✅ | 博主名称（搜索关键词） |
| retry_count | int | ❌ | 失败重试次数（默认3次） |
| timeout | int | ❌ | 超时时间（秒，默认60） |

### collect_comments 参数（v0.3）
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| blogger_name | string | ✅ | 博主名称 |
| video_count | int | ❌ | 采集作品数量（默认10） |
| comment_count | int | ❌ | 每个作品采集评论数（默认50） |
| filter_keywords | array | ❌ | 过滤关键词 |

### send_message 参数（v0.4）
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| user_id | string | ✅ | 目标用户ID |
| message | string | ✅ | 消息内容 |
| delay | int | ❌ | 发送延迟（秒，默认5） |

---

## 📊 任务状态说明

| 状态 | 说明 |
|------|------|
| pending | 等待执行 |
| running | 执行中 |
| completed | 执行成功 |
| failed | 执行失败 |
| cancelled | 已取消 |

---

## 🚀 使用方式

### 方式1：手动编辑任务
1. 在本文档中添加新任务
2. 服务器定期读取此文档
3. 按优先级执行任务
4. 更新任务状态

### 方式2：通过API添加任务
```python
# 服务器端代码示例
task = {
    "task_id": "task_004",
    "type": "search_blogger",
    "status": "pending",
    "priority": 4,
    "config": {
        "blogger_name": "测试博主",
        "retry_count": 3,
        "timeout": 60
    },
    "created_at": datetime.now().isoformat()
}

# 发送到App
await websocket.send_json({
    "type": "search_blogger",
    "blogger_name": task["config"]["blogger_name"],
    "task_id": task["task_id"]
})
```

### 方式3：批量导入任务
```yaml
# tasks.yaml
tasks:
  - task_id: task_001
    type: search_blogger
    config:
      blogger_name: "罗翔说刑法"
  
  - task_id: task_002
    type: search_blogger
    config:
      blogger_name: "李永乐老师"
```

---

## 📈 任务执行流程

```
1. 服务器读取任务配置
   ↓
2. 按优先级排序
   ↓
3. 检查App连接状态
   ↓
4. 发送任务到App
   ↓
5. App执行任务
   ↓
6. App上报执行结果
   ↓
7. 服务器更新任务状态
   ↓
8. 记录执行日志
```

---

## 🔍 任务执行日志

### 任务 #1 执行记录
```
[2026-05-15 12:00:00] 任务创建
[2026-05-15 12:01:00] 发送到App
[2026-05-15 12:01:05] App开始执行
[2026-05-15 12:01:10] 启动抖音成功
[2026-05-15 12:01:15] 搜索博主成功
[2026-05-15 12:01:20] 进入主页成功
[2026-05-15 12:01:25] 任务完成
```

---

## ⚙️ 高级配置

### 任务调度策略
```yaml
scheduler:
  mode: sequential  # sequential(顺序) / parallel(并行)
  max_concurrent: 1  # 最大并发任务数
  retry_delay: 60    # 重试延迟（秒）
  max_retries: 3     # 最大重试次数
```

### 限流配置
```yaml
rate_limit:
  search_per_hour: 10      # 每小时搜索次数
  collect_per_hour: 5      # 每小时采集次数
  message_per_hour: 20     # 每小时私信次数
  delay_between_tasks: 30  # 任务间隔（秒）
```

### 错误处理
```yaml
error_handling:
  auto_retry: true           # 自动重试
  notify_on_failure: true    # 失败通知
  fallback_strategy: skip    # skip(跳过) / retry(重试) / abort(中止)
```

---

## 📝 任务模板

### 搜索博主模板
```yaml
task_id: task_xxx
type: search_blogger
status: pending
priority: 1
config:
  blogger_name: "博主名称"
  retry_count: 3
  timeout: 60
created_at: "YYYY-MM-DD HH:MM:SS"
```

### 采集评论模板（v0.3）
```yaml
task_id: task_xxx
type: collect_comments
status: pending
priority: 1
config:
  blogger_name: "博主名称"
  video_count: 10
  comment_count: 50
  filter_keywords: ["关键词1", "关键词2"]
created_at: "YYYY-MM-DD HH:MM:SS"
```

### 发送私信模板（v0.4）
```yaml
task_id: task_xxx
type: send_message
status: pending
priority: 1
config:
  user_id: "用户ID"
  message: "消息内容"
  delay: 5
created_at: "YYYY-MM-DD HH:MM:SS"
```

---

## 🎯 待执行任务队列

当前队列中的任务将按优先级顺序执行：

1. ✅ task_001 - 搜索"罗翔说刑法"
2. ⏳ task_002 - 搜索"李永乐老师"
3. ⏳ task_003 - 搜索"半佛仙人"

---

**最后更新：** 2026-05-15 13:00:00  
**文档版本：** 1.0  
**维护者：** 开发团队
