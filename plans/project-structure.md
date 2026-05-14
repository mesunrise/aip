# 抖音自动化项目代码结构

## 📁 项目目录结构

```
douyin-automation/
├── README.md                      # 项目说明
├── requirements.txt               # Python依赖
├── config.yaml                    # 配置文件
├── .env.example                   # 环境变量模板
├── .gitignore                     # Git忽略文件
│
├── src/                          # 源代码目录
│   ├── __init__.py
│   │
│   ├── core/                     # 核心模块
│   │   ├── __init__.py
│   │   ├── device.py            # 设备管理
│   │   ├── douyin.py            # 抖音操作封装
│   │   └── ai_helper.py         # AI辅助模块
│   │
│   ├── collectors/               # 采集模块
│   │   ├── __init__.py
│   │   ├── comment_collector.py # 评论采集器
│   │   ├── user_extractor.py    # 用户信息提取
│   │   └── video_parser.py      # 视频解析
│   │
│   ├── senders/                  # 发送模块
│   │   ├── __init__.py
│   │   ├── message_sender.py    # 私信发送器
│   │   └── template_engine.py   # 消息模板引擎
│   │
│   ├── storage/                  # 存储模块
│   │   ├── __init__.py
│   │   ├── database.py          # 数据库操作
│   │   ├── models.py            # 数据模型
│   │   └── cache.py             # 缓存管理
│   │
│   ├── utils/                    # 工具模块
│   │   ├── __init__.py
│   │   ├── anti_detection.py    # 反检测
│   │   ├── rate_limiter.py      # 频率限制
│   │   ├── ocr.py               # OCR识别
│   │   └── logger.py            # 日志工具
│   │
│   ├── scheduler/                # 调度模块
│   │   ├── __init__.py
│   │   ├── task_scheduler.py    # 任务调度器
│   │   └── job_manager.py       # 任务管理
│   │
│   └── api/                      # API接口
│       ├── __init__.py
│       ├── app.py               # FastAPI应用
│       └── routes/              # 路由
│           ├── __init__.py
│           ├── tasks.py         # 任务接口
│           └── reports.py       # 报告接口
│
├── scripts/                      # 脚本目录
│   ├── init_db.py               # 初始化数据库
│   ├── test_device.py           # 测试设备连接
│   └── run_task.py              # 运行任务
│
├── tests/                        # 测试目录
│   ├── __init__.py
│   ├── test_collector.py
│   ├── test_sender.py
│   └── test_utils.py
│
├── data/                         # 数据目录
│   ├── douyin.db                # SQLite数据库
│   └── cache/                   # 缓存文件
│
├── logs/                         # 日志目录
│   ├── app.log
│   ├── error.log
│   └── audit.log
│
├── reports/                      # 报告目录
│   └── daily/                   # 每日报告
│
└── docs/                         # 文档目录
    ├── API.md                   # API文档
    ├── DEPLOYMENT.md            # 部署文档
    └── FAQ.md                   # 常见问题
```

## 🔧 核心模块说明

### 1. 设备管理 (device.py)

```python
"""
功能：
- ADB设备连接
- 屏幕截图
- 点击、滑动操作
- 输入文本
"""

主要类：
- DeviceManager: 设备管理器
- AndroidDevice: Android设备封装
```

### 2. 抖音操作 (douyin.py)

```python
"""
功能：
- 登录抖音
- 搜索博主
- 浏览作品
- 查看评论
- 发送私信
"""

主要类：
- DouyinClient: 抖音客户端
- DouyinNavigator: 导航器
```

### 3. 评论采集 (comment_collector.py)

```python
"""
功能：
- 遍历作品列表
- 滚动加载评论
- 提取用户信息
- 数据去重
"""

主要类：
- CommentCollector: 评论采集器
- CommentParser: 评论解析器
```

### 4. 私信发送 (message_sender.py)

```python
"""
功能：
- 批量发送私信
- 消息模板渲染
- 发送状态跟踪
- 失败重试
"""

主要类：
- MessageSender: 私信发送器
- MessageQueue: 消息队列
```

### 5. 反检测 (anti_detection.py)

```python
"""
功能：
- 随机延迟
- 行为模拟
- 设备指纹
- 风控检测
"""

主要类：
- AntiDetection: 反检测策略
- BehaviorSimulator: 行为模拟器
```

## 📦 依赖包说明

### requirements.txt

```txt
# 核心框架
fastapi==0.109.0
uvicorn[standard]==0.27.0
pydantic==2.5.0

# 自动化
uiautomator2==3.0.0
pure-python-adb==0.3.0.dev0

# 图像处理
Pillow==10.2.0
opencv-python==4.9.0
numpy==1.26.3

# OCR
paddleocr==2.7.0
paddlepaddle==2.6.0

# AI模型（可选）
openai==1.10.0
anthropic==0.18.0

# 数据库
sqlalchemy==2.0.25
alembic==1.13.1

# 任务调度
apscheduler==3.10.4

# 工具
python-dotenv==1.0.0
PyYAML==6.0.1
loguru==0.7.2
click==8.1.7
rich==13.7.0

# HTTP
httpx==0.26.0
aiohttp==3.9.1

# 测试
pytest==7.4.4
pytest-asyncio==0.23.3
pytest-cov==4.1.0
```

## 🚀 快速开始

### 1. 安装依赖

```bash
# 创建虚拟环境
python -m venv venv
source venv/bin/activate  # Linux/Mac
# 或
venv\Scripts\activate  # Windows

# 安装依赖
pip install -r requirements.txt
```

### 2. 配置项目

```bash
# 复制配置文件
cp config.example.yaml config.yaml

# 编辑配置
vim config.yaml
```

### 3. 初始化数据库

```bash
python scripts/init_db.py
```

### 4. 测试设备连接

```bash
python scripts/test_device.py
```

### 5. 运行任务

```bash
# 命令行方式
python scripts/run_task.py --blogger "美食博主" --max-videos 10

# API方式
python -m src.api.app
# 访问 http://localhost:8000/docs
```

## 📝 配置文件示例

### config.yaml

```yaml
# 抖音账号
douyin:
  accounts:
    - phone: "13800138000"
      password: "your_password"

# 目标博主
targets:
  - name: "美食博主A"
    max_videos: 20

# 私信模板
message:
  template: "你好，看到你的评论..."

# 频率限制
rate_limit:
  messages_per_day: 50
```

## 🔐 环境变量

### .env

```bash
# 数据库
DATABASE_URL=sqlite:///data/douyin.db

# AI API密钥（可选）
OPENAI_API_KEY=sk-xxx
ANTHROPIC_API_KEY=sk-ant-xxx

# 日志级别
LOG_LEVEL=INFO

# API端口
API_PORT=8000
```

## 📊 数据库迁移

```bash
# 创建迁移
alembic revision --autogenerate -m "Initial migration"

# 执行迁移
alembic upgrade head

# 回滚
alembic downgrade -1
```

## 🧪 运行测试

```bash
# 运行所有测试
pytest

# 运行特定测试
pytest tests/test_collector.py

# 生成覆盖率报告
pytest --cov=src --cov-report=html
```

## 📈 监控和日志

### 日志文件

```
logs/
├── app.log          # 应用日志
├── error.log        # 错误日志
└── audit.log        # 审计日志
```

### 查看日志

```bash
# 实时查看
tail -f logs/app.log

# 查看错误
tail -f logs/error.log
```

## 🔄 CI/CD

### GitHub Actions 示例

```yaml
name: CI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up Python
        uses: actions/setup-python@v2
        with:
          python-version: 3.9
      - name: Install dependencies
        run: |
          pip install -r requirements.txt
      - name: Run tests
        run: pytest
```

## 📚 开发指南

### 代码规范

- 使用 Black 格式化代码
- 使用 Flake8 检查代码质量
- 使用 MyPy 进行类型检查
- 编写单元测试

### Git 工作流

```bash
# 创建功能分支
git checkout -b feature/new-feature

# 提交代码
git add .
git commit -m "feat: add new feature"

# 推送分支
git push origin feature/new-feature

# 创建 Pull Request
```

## 🆘 故障排查

### 常见问题

1. **设备连接失败**
   ```bash
   adb devices
   adb kill-server
   adb start-server
   ```

2. **数据库锁定**
   ```bash
   rm data/douyin.db-journal
   ```

3. **依赖安装失败**
   ```bash
   pip install --upgrade pip
   pip install -r requirements.txt --no-cache-dir
   ```

## 📖 相关文档

- [完整技术方案](douyin-automation-plan.md)
- [API文档](../docs/API.md)
- [部署文档](../docs/DEPLOYMENT.md)
- [常见问题](../docs/FAQ.md)

---

**准备好开始开发了吗？** 

建议切换到 **Code 模式** 开始实现核心功能！
