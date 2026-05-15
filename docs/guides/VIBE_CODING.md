# Vibe Coding 规范

## 项目原则

### 1. 最小化原则
- 只写必要的代码
- 避免过度设计
- 先实现核心功能

### 2. 快速迭代
- 小步快跑
- 快速验证
- 持续重构

### 3. 代码质量
- 清晰命名
- 简洁实现
- 必要注释

## 项目结构

```
douyin-automation/
├── android-app/          # Android应用
├── cloud-server/         # 云端服务器
├── docs/                 # 文档
├── plans/                # 规划文档
└── shared/               # 共享代码
```

## 开发流程

### v0.1 开发步骤

1. **创建项目结构**
2. **实现最小可用功能**
3. **测试验证**
4. **文档更新**

## 代码规范

### Kotlin (Android)
```kotlin
// 简洁、直接
class WebSocketClient(private val url: String) {
    fun connect() { /* 实现 */ }
    fun send(message: String) { /* 实现 */ }
}
```

### Python (后端)
```python
# 简洁、直接
class WebSocketServer:
    async def handle_connection(self, websocket):
        pass
```

## Git提交规范

```bash
feat(v0.1): 实现WebSocket客户端
fix(v0.1): 修复连接断开问题
docs: 更新README
```

## 测试策略

- 手动测试为主
- 关键功能单元测试
- 端到端测试

## 文档要求

- README.md - 项目说明
- SETUP.md - 环境配置
- API.md - 接口文档（如需要）
