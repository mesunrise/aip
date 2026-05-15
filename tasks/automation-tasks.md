# 抖音自动化任务配置

## 📋 任务配置说明

本文档用于配置抖音自动化任务。服务器会读取此配置并按顺序执行任务。

## 🎯 任务类型

### 1. 搜索博主任务 (search_blogger)
搜索指定博主并进入主页

### 2. 搜索作品任务 (search_video)
搜索关键词，进入第一个作品的博主主页

### 3. 采集评论任务 (collect_comments)
采集指定作品的评论（v0.3实现）

### 4. 发送私信任务 (send_message)
向指定用户发送私信（v0.4实现）

---

## 📝 当前任务列表

### 测试任务1：搜索"妮子"并进入作品
```yaml
task_id: test_task_001
task_name: "测试任务1"
type: search_and_explore
status: pending
priority: 1
config:
  search_keyword: "妮子"
  steps:
    - action: search_keyword
      keyword: "妮子"
      description: "搜索关键词'妮子'"
    
    - action: enter_first_video_author
      description: "进入搜索到的第一个作品的博主主页"
    
    - action: scroll_profile
      scroll_count: 3
      description: "在博主主页向下滑动3次"
    
    - action: enter_first_video
      description: "进入博主的第一个作品主页"
    
    - action: return_to_app
      description: "返回到App主界面"
  
  retry_count: 3
  timeout: 180
  
created_at: "2026-05-15 13:00:00"
```

**任务详细说明：**

**步骤1：搜索关键词**
- 启动抖音
- 点击搜索按钮
- 输入"妮子"
- 触发搜索

**步骤2：进入第一个作品的博主主页**
- 等待搜索结果加载
- 点击第一个作品
- 进入作品播放页面
- 点击博主头像
- 进入博主主页

**步骤3：滑动博主主页**
- 在博主主页向下滑动
- 滑动3次，查看更多作品
- 每次滑动间隔1秒

**步骤4：进入博主的第一个作品**
- 点击作品列表中的第一个作品
- 进入作品播放页面

**步骤5：返回到App主界面**
- 按返回键退出作品页面
- 按返回键退出博主主页
- 按返回键退出搜索结果
- 返回到App主界面

---

### 任务 #2: 搜索博主 - 罗翔说刑法（备用）
```yaml
task_id: task_002
type: search_blogger
status: pending
priority: 2
config:
  blogger_name: "罗翔说刑法"
  retry_count: 3
  timeout: 60
created_at: "2026-05-15 12:05:00"
```

**任务说明：**
- 搜索博主"罗翔说刑法"
- 进入博主主页
- 验证是否成功进入
- 上报执行结果

---

## 🔧 任务配置参数说明

### 通用参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| task_id | string | ✅ | 任务唯一标识 |
| task_name | string | ❌ | 任务名称（便于识别） |
| type | string | ✅ | 任务类型 |
| status | string | ✅ | 任务状态：pending/running/completed/failed |
| priority | int | ✅ | 优先级（数字越小优先级越高） |
| created_at | string | ✅ | 创建时间 |

### search_and_explore 参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| search_keyword | string | ✅ | 搜索关键词 |
| steps | array | ✅ | 执行步骤列表 |
| retry_count | int | ❌ | 失败重试次数（默认3次） |
| timeout | int | ❌ | 超时时间（秒，默认180） |

### 步骤动作类型
| 动作 | 说明 | 参数 |
|------|------|------|
| search_keyword | 搜索关键词 | keyword |
| enter_first_video_author | 进入第一个作品的博主主页 | - |
| scroll_profile | 滑动博主主页 | scroll_count |
| enter_first_video | 进入第一个作品 | - |
| return_to_app | 返回App主界面 | - |

### search_blogger 参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| blogger_name | string | ✅ | 博主名称（搜索关键词） |
| retry_count | int | ❌ | 失败重试次数（默认3次） |
| timeout | int | ❌ | 超时时间（秒，默认60） |

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
    "task_id": "test_task_002",
    "task_name": "测试任务2",
    "type": "search_and_explore",
    "status": "pending",
    "priority": 1,
    "config": {
        "search_keyword": "测试关键词",
        "steps": [
            {"action": "search_keyword", "keyword": "测试关键词"},
            {"action": "enter_first_video_author"},
            {"action": "scroll_profile", "scroll_count": 3},
            {"action": "enter_first_video"},
            {"action": "return_to_app"}
        ]
    },
    "created_at": datetime.now().isoformat()
}

# 发送到App
await websocket.send_json({
    "type": "search_and_explore",
    "task_id": task["task_id"],
    **task["config"]
})
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
5. App执行任务步骤
   ↓
6. 每个步骤完成后上报进度
   ↓
7. 所有步骤完成后上报最终结果
   ↓
8. 服务器更新任务状态
   ↓
9. 记录执行日志
```

---

## 🔍 任务执行日志示例

### 测试任务1 执行记录
```
[2026-05-15 13:00:00] 任务创建
[2026-05-15 13:01:00] 发送到App
[2026-05-15 13:01:05] App开始执行
[2026-05-15 13:01:10] ✅ 步骤1: 搜索关键词"妮子" - 成功
[2026-05-15 13:01:20] ✅ 步骤2: 进入第一个作品的博主主页 - 成功
[2026-05-15 13:01:30] ✅ 步骤3: 滑动博主主页 - 成功
[2026-05-15 13:01:40] ✅ 步骤4: 进入第一个作品 - 成功
[2026-05-15 13:01:45] ✅ 步骤5: 返回到App主界面 - 成功
[2026-05-15 13:01:50] 任务完成
```

---

## 📝 任务模板

### 搜索并探索模板
```yaml
task_id: test_task_xxx
task_name: "任务名称"
type: search_and_explore
status: pending
priority: 1
config:
  search_keyword: "关键词"
  steps:
    - action: search_keyword
      keyword: "关键词"
    - action: enter_first_video_author
    - action: scroll_profile
      scroll_count: 3
    - action: enter_first_video
    - action: return_to_app
  retry_count: 3
  timeout: 180
created_at: "YYYY-MM-DD HH:MM:SS"
```

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

---

## 🎯 待执行任务队列

当前队列中的任务将按优先级顺序执行：

1. ⏳ test_task_001 - 搜索"妮子"并探索
2. ⏳ task_002 - 搜索"罗翔说刑法"

---

**最后更新：** 2026-05-15 13:15:00  
**文档版本：** 2.0  
**维护者：** 开发团队
