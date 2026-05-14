"""
简单任务示例 - 演示基本的手机AI自动化功能

这个示例展示如何：
1. 连接到Android设备
2. 执行简单任务
3. 查看执行结果
"""

import asyncio
from pathlib import Path
import sys

# 添加项目根目录到路径
sys.path.insert(0, str(Path(__file__).parent.parent))


async def example_open_app():
    """示例：打开应用"""
    print("🚀 示例1：打开微信应用")
    print("-" * 50)
    
    # 这里是伪代码，展示API使用方式
    # 实际使用时需要先实现核心模块
    
    """
    from src.core import MobileAgent
    
    # 创建Agent
    agent = MobileAgent(config_path="config.yaml")
    
    # 连接设备
    device = await agent.connect_device()
    print(f"✅ 已连接设备: {device.id}")
    
    # 执行任务
    result = await agent.execute_task(
        device=device,
        task="打开微信"
    )
    
    print(f"📊 执行结果: {result.status}")
    print(f"⏱️  执行时间: {result.duration}秒")
    """
    
    print("📝 这是一个示例，实际实现请参考核心模块")
    print()


async def example_complex_task():
    """示例：复杂多步骤任务"""
    print("🚀 示例2：发送微信消息")
    print("-" * 50)
    
    """
    from src.core import MobileAgent
    
    agent = MobileAgent(config_path="config.yaml")
    device = await agent.connect_device()
    
    # 执行复杂任务
    task = '''
    1. 打开微信
    2. 点击搜索框
    3. 输入"张三"
    4. 点击第一个联系人
    5. 输入消息"你好，周末一起吃饭吗？"
    6. 点击发送按钮
    '''
    
    result = await agent.execute_task(
        device=device,
        task=task
    )
    
    # 查看执行详情
    for step in result.steps:
        print(f"  步骤 {step.index}: {step.action} - {step.status}")
    
    print(f"✅ 任务完成，共执行 {len(result.steps)} 步")
    """
    
    print("📝 这是一个示例，展示多步骤任务执行流程")
    print()


async def example_with_retry():
    """示例：带错误恢复的任务"""
    print("🚀 示例3：带重试机制的任务")
    print("-" * 50)
    
    """
    from src.core import MobileAgent
    from src.core.executor import ExecutionConfig
    
    agent = MobileAgent(config_path="config.yaml")
    device = await agent.connect_device()
    
    # 配置执行选项
    config = ExecutionConfig(
        max_retries=3,
        retry_delay=2.0,
        error_recovery_enabled=True
    )
    
    result = await agent.execute_task(
        device=device,
        task="打开淘宝并搜索iPhone 15",
        config=config
    )
    
    if result.status == "success":
        print("✅ 任务成功完成")
    elif result.status == "failed":
        print(f"❌ 任务失败: {result.error}")
        print(f"🔄 已重试 {result.retry_count} 次")
    """
    
    print("📝 演示了错误处理和重试机制")
    print()


async def example_batch_tasks():
    """示例：批量任务执行"""
    print("🚀 示例4：批量执行多个任务")
    print("-" * 50)
    
    """
    from src.core import MobileAgent
    
    agent = MobileAgent(config_path="config.yaml")
    device = await agent.connect_device()
    
    # 定义多个任务
    tasks = [
        "打开微信，查看未读消息数量",
        "打开抖音，刷5个视频",
        "打开支付宝，查看余额",
    ]
    
    # 批量执行
    results = await agent.execute_batch(
        device=device,
        tasks=tasks
    )
    
    # 统计结果
    success_count = sum(1 for r in results if r.status == "success")
    print(f"📊 完成 {success_count}/{len(tasks)} 个任务")
    
    for i, result in enumerate(results):
        status_icon = "✅" if result.status == "success" else "❌"
        print(f"  {status_icon} 任务{i+1}: {result.status}")
    """
    
    print("📝 展示了如何批量执行多个任务")
    print()


async def example_custom_actions():
    """示例：自定义操作序列"""
    print("🚀 示例5：自定义操作序列")
    print("-" * 50)
    
    """
    from src.core import MobileAgent
    from src.core.executor import Action
    
    agent = MobileAgent(config_path="config.yaml")
    device = await agent.connect_device()
    
    # 定义自定义操作序列
    actions = [
        Action(type="tap", x=500, y=1000),
        Action(type="wait", duration=1.0),
        Action(type="swipe", start=(500, 1500), end=(500, 500)),
        Action(type="input_text", text="Hello World"),
    ]
    
    # 执行操作序列
    for action in actions:
        result = await agent.execute_action(device, action)
        print(f"  {action.type}: {result.status}")
    """
    
    print("📝 展示了如何执行自定义操作序列")
    print()


async def main():
    """主函数"""
    print("=" * 60)
    print("   📱 Mobile AI Agent - 使用示例")
    print("=" * 60)
    print()
    
    # 运行各个示例
    await example_open_app()
    await example_complex_task()
    await example_with_retry()
    await example_batch_tasks()
    await example_custom_actions()
    
    print("=" * 60)
    print("🎉 示例演示完成！")
    print()
    print("💡 提示：")
    print("  1. 这些是伪代码示例，展示API使用方式")
    print("  2. 实际使用前需要先实现核心模块")
    print("  3. 请参考 docs/ 目录中的详细文档")
    print("  4. 配置文件示例: config.example.yaml")
    print("=" * 60)


if __name__ == "__main__":
    # 运行示例
    asyncio.run(main())
