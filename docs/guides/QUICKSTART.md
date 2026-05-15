# 🚀 快速开始指南

本指南将帮助你在5分钟内开始使用 Mobile AI Agent 进行手机自动化。

## 📋 前置条件

### 1. 系统要求
- Python 3.9 或更高版本
- 8GB+ RAM
- Windows/Linux/macOS

### 2. Android设备准备
```bash
# 启用开发者选项
设置 → 关于手机 → 连续点击"版本号"7次

# 启用USB调试
设置 → 开发者选项 → USB调试（打开）

# 验证连接
adb devices
# 应该看到你的设备ID
```

### 3. 安装ADB工具

**Windows:**
```bash
# 下载并安装 Android SDK Platform Tools
# https://developer.android.com/studio/releases/platform-tools
```

**Linux/macOS:**
```bash
# Ubuntu/Debian
sudo apt install android-tools-adb

# macOS
brew install android-platform-tools
```

## 📦 安装步骤

### Step 1: 克隆项目
```bash
git clone https://github.com/yourusername/mobile-ai-agent.git
cd mobile-ai-agent
```

### Step 2: 创建虚拟环境
```bash
# 创建虚拟环境
python -m venv venv

# 激活虚拟环境
# Windows:
venv\Scripts\activate
# Linux/macOS:
source venv/bin/activate
```

### Step 3: 安装依赖
```bash
pip install -r requirements.txt
```

### Step 4: 配置
```bash
# 复制配置模板
cp config.example.yaml config.yaml

# 编辑配置文件，至少需要配置：
# 1. AI模型API密钥（OpenAI/Anthropic等）
# 2. 设备连接方式
```

**最小配置示例：**
```yaml
ai_model:
  provider: "openai"
  openai:
    api_key: "sk-your-api-key-here"  # 👈 填入你的API密钥
    model: "gpt-4-vision-preview"

device:
  connection_type: "adb"

logging:
  level: "INFO"
```

## 🎯 第一个任务

### 方式1：使用Python API

创建文件 `my_first_task.py`:

```python
import asyncio
from src.core import MobileAgent

async def main():
    # 创建Agent
    agent = MobileAgent(config_path="config.yaml")
    
    # 连接设备
    device = await agent.connect_device()
    print(f"✅ 已连接设备: {device.id}")
    
    # 执行简单任务
    result = await agent.execute_task(
        device=device,
        task="打开设置应用"
    )
    
    print(f"任务状态: {result.status}")
    print(f"执行时间: {result.duration}秒")

if __name__ == "__main__":
    asyncio.run(main())
```

运行：
```bash
python my_first_task.py
```

### 方式2：使用CLI

```bash
# 列出可用设备
python -m src.cli devices list

# 执行任务
python -m src.cli task execute \
    --task "打开设置应用"
```

## 📱 更多示例

### 示例1：打开应用并搜索
```python
task = "打开淘宝，搜索'iPhone 15'"
result = await agent.execute_task(device, task)
```

### 示例2：发送消息
```python
task = """
1. 打开微信
2. 搜索联系人"张三"
3. 发送消息"你好"
"""
result = await agent.execute_task(device, task)
```

### 示例3：批量操作
```python
tasks = [
    "打开微信，查看未读消息",
    "打开支付宝，查看余额",
    "打开抖音，刷3个视频"
]

results = await agent.execute_batch(device, tasks)
```

## 🔧 常见问题

### Q1: 找不到设备
```bash
# 检查ADB连接
adb devices

# 如果显示"未授权"，在手机上允许USB调试
# 如果显示"offline"，重启ADB服务
adb kill-server
adb start-server
```

### Q2: API密钥错误
```yaml
# 确保config.yaml中的API密钥正确
ai_model:
  provider: "openai"
  openai:
    api_key: "sk-..."  # 检查此处
```

### Q3: 模块导入错误
```bash
# 确保在项目根目录运行
cd mobile-ai-agent

# 确保虚拟环境已激活
source venv/bin/activate  # Linux/macOS
venv\Scripts\activate     # Windows
```

### Q4: 任务执行失败
```python
# 启用调试模式查看详细日志
import logging
logging.basicConfig(level=logging.DEBUG)
```

## 📊 查看执行日志

```bash
# 实时查看日志
tail -f logs/agent.log

# 查看审计日志
tail -f logs/audit.log
```

## 🎓 学习资源

### 文档
- [项目深度分析](mobile-ai-automation-analysis.md) - 了解技术背景
- [技术架构](technical-architecture.md) - 理解系统设计
- [关键技术](key-technologies.md) - 掌握核心技术

### 示例
- [简单任务示例](examples/simple_task.py) - 基础用法
- [高级示例](examples/) - 更多复杂场景

### API文档
```python
# 查看帮助
python -m src.cli --help

# 查看特定命令帮助
python -m src.cli task --help
```

## 💡 最佳实践

### 1. 任务设计
```python
# ✅ 好的任务描述 - 清晰明确
task = "打开微信，点击搜索框，输入'张三'，点击第一个搜索结果"

# ❌ 不好的任务描述 - 模糊不清
task = "在微信里找到张三"
```

### 2. 错误处理
```python
try:
    result = await agent.execute_task(device, task)
    if result.status == "success":
        print("✅ 成功")
    else:
        print(f"❌ 失败: {result.error}")
except Exception as e:
    print(f"⚠️  异常: {e}")
```

### 3. 性能优化
```python
# 使用缓存
agent = MobileAgent(
    config_path="config.yaml",
    cache_enabled=True
)

# 调整延迟
config = ExecutionConfig(
    step_delay=0.5  # 减少延迟提高速度
)
```

## 🎯 下一步

1. **探索示例** - 查看 `examples/` 目录中的更多示例
2. **阅读文档** - 深入了解各个模块的功能
3. **自定义任务** - 编写适合你需求的自动化任务
4. **贡献代码** - 欢迎提交PR改进项目

## 🆘 获取帮助

- 📖 查看完整文档: `docs/`
- 🐛 报告问题: [GitHub Issues](https://github.com/yourusername/mobile-ai-agent/issues)
- 💬 加入讨论: [Discussions](https://github.com/yourusername/mobile-ai-agent/discussions)

---

**祝你使用愉快！** 🎉

如有问题，请查看[常见问题文档](FAQ.md)或在GitHub上提issue。
