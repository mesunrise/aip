# 项目长期规划与最佳实践

## 📁 项目结构规范

### 1. 文档组织结构

```
plans/
├── README.md                          # 项目总览和导航
├── development-roadmap.md             # 完整开发路线图
├── architecture.md                    # 架构设计文档
├── best-practices.md                  # 最佳实践指南
├── v0.1/                             # v0.1版本文档
│   ├── requirements.md               # 需求文档
│   ├── tasks.md                      # 任务清单
│   ├── test-report.md                # 测试报告
│   ├── changelog.md                  # 变更日志
│   └── lessons-learned.md            # 经验总结
├── v0.2/                             # v0.2版本文档
│   ├── requirements.md
│   ├── tasks.md
│   ├── test-report.md
│   ├── changelog.md
│   └── lessons-learned.md
├── v0.3/                             # v0.3版本文档
│   └── ...
└── ...
```

### 2. 代码组织结构

```
android-app/app/src/main/java/com/douyin/automation/
├── core/                             # 核心模块（稳定，少修改）
│   ├── base/                         # 基础类
│   ├── network/                      # 网络通信
│   └── utils/                        # 工具类
├── accessibility/                    # 无障碍服务（v0.2引入）
│   ├── AutomationAccessibilityService.kt
│   └── AccessibilityHelper.kt
├── douyin/                           # 抖音操作（v0.2引入）
│   ├── DouyinLauncher.kt
│   ├── DouyinNavigator.kt
│   └── DouyinElements.kt
├── locator/                          # 元素定位（v0.2引入）
│   └── ElementLocator.kt
├── collector/                        # 数据采集（v0.3引入）
│   └── ...
├── sender/                           # 消息发送（v0.4引入）
│   └── ...
└── MainActivity.kt                   # 主界面（每版本可能更新）
```

## 🛡️ 版本隔离机制

### 1. 模块化设计原则

#### 核心原则：
- **单一职责**：每个模块只负责一个功能
- **接口隔离**：模块间通过接口通信
- **依赖倒置**：高层模块不依赖低层模块
- **开闭原则**：对扩展开放，对修改关闭

#### 实施方案：

**方案1：功能模块化**
```kotlin
// v0.2引入的模块
package com.douyin.automation.v02.search

interface BloggerSearcher {
    suspend fun search(name: String): SearchResult
}

class DouyinBloggerSearcher : BloggerSearcher {
    override suspend fun search(name: String): SearchResult {
        // 实现
    }
}
```

**方案2：版本命名空间**
```kotlin
// 明确标注版本
package com.douyin.automation.v02.douyin
package com.douyin.automation.v03.collector
package com.douyin.automation.v04.sender
```

**方案3：Feature Toggle（功能开关）**
```kotlin
object FeatureFlags {
    const val ENABLE_SEARCH = true      // v0.2
    const val ENABLE_COLLECT = false    // v0.3
    const val ENABLE_SEND = false       // v0.4
}
```

### 2. 文件修改保护机制

#### 方案1：Git分支策略
```bash
main                    # 稳定版本
├── v0.1-stable        # v0.1稳定分支（只修复bug）
├── v0.2-stable        # v0.2稳定分支
├── v0.2-dev           # v0.2开发分支
├── v0.3-dev           # v0.3开发分支
└── feature/xxx        # 功能分支
```

#### 方案2：代码审查规则
```yaml
# .github/CODEOWNERS
# v0.1核心文件保护
/android-app/app/src/main/java/com/douyin/automation/network/  @team-lead
/android-app/app/src/main/java/com/douyin/automation/core/     @team-lead

# v0.2文件可由开发者修改
/android-app/app/src/main/java/com/douyin/automation/douyin/   @developers
```

#### 方案3：文件锁定标记
```kotlin
/**
 * @version v0.1
 * @stable true
 * @warning 此文件已稳定，修改需经过团队审查
 */
class WebSocketClient {
    // ...
}
```

### 3. 依赖管理

#### 版本依赖图：
```
v0.1: WebSocket通信
  ↓
v0.2: 博主定位（依赖v0.1）
  ↓
v0.3: 评论采集（依赖v0.1, v0.2）
  ↓
v0.4: 私信发送（依赖v0.1, v0.3）
  ↓
v0.5: 性能优化（依赖所有）
  ↓
v1.0: 正式发布
```

#### 依赖声明：
```kotlin
// v0.3模块
dependencies {
    implementation(project(":core"))           // v0.1核心
    implementation(project(":accessibility"))  // v0.2无障碍
    implementation(project(":douyin"))         // v0.2抖音操作
}
```

## 📋 版本开发流程

### 1. 版本启动阶段

#### Step 1: 创建版本文档
```bash
mkdir plans/v0.x
cd plans/v0.x
touch requirements.md tasks.md changelog.md
```

#### Step 2: 需求分析
- 明确版本目标
- 列出功能清单
- 识别依赖关系
- 评估风险

#### Step 3: 设计评审
- 架构设计
- 接口设计
- 数据结构设计
- 与现有模块的集成方案

### 2. 版本开发阶段

#### Step 1: 创建功能分支
```bash
git checkout -b v0.x-dev
```

#### Step 2: 模块化开发
- 创建独立的包/模块
- 定义清晰的接口
- 编写单元测试
- 文档同步更新

#### Step 3: 集成测试
- 与现有模块集成
- 端到端测试
- 性能测试

### 3. 版本发布阶段

#### Step 1: 代码审查
- 检查代码质量
- 确认没有修改不该修改的文件
- 验证测试覆盖率

#### Step 2: 文档完善
- 更新changelog.md
- 编写test-report.md
- 总结lessons-learned.md

#### Step 3: 版本标记
```bash
git tag v0.x
git push origin v0.x
```

## 🔒 文件修改规则

### 1. 核心文件（禁止修改）

**v0.1核心文件：**
```
✅ 可读取
❌ 禁止修改
⚠️ 修改需审批

android-app/app/src/main/java/com/douyin/automation/
├── network/WebSocketClient.kt          ⚠️
├── network/MessageHandler.kt           ⚠️
└── core/                               ⚠️
```

**修改流程：**
1. 提交修改申请
2. 说明修改原因
3. 评估影响范围
4. 团队审查批准
5. 创建兼容性测试

### 2. 版本文件（可修改）

**v0.2文件：**
```
✅ 可自由修改
📝 需要文档

android-app/app/src/main/java/com/douyin/automation/
├── accessibility/                      ✅
├── douyin/                            ✅
├── locator/                           ✅
├── logger/                            ✅
├── permission/                        ✅
└── task/                              ✅
```

### 3. 共享文件（谨慎修改）

**MainActivity.kt：**
```kotlin
// ⚠️ 共享文件，多版本都会修改
// 规则：只添加，不删除，不修改现有功能

class MainActivity : AppCompatActivity() {
    
    // v0.1功能 - 不要修改
    private fun connectToServer() { }
    
    // v0.2功能 - 可以修改
    private fun checkAccessibility() { }
    
    // v0.3功能 - 新增
    private fun startCollecting() { }
}
```

## 📊 版本管理工具

### 1. 版本检查脚本

**check-version-changes.sh：**
```bash
#!/bin/bash
# 检查是否修改了不该修改的文件

PROTECTED_FILES=(
    "android-app/app/src/main/java/com/douyin/automation/network/WebSocketClient.kt"
    "android-app/app/src/main/java/com/douyin/automation/network/MessageHandler.kt"
)

for file in "${PROTECTED_FILES[@]}"; do
    if git diff --name-only HEAD | grep -q "$file"; then
        echo "⚠️ 警告: 修改了受保护的文件 $file"
        echo "请确认这是必要的修改，并获得审批"
        exit 1
    fi
done

echo "✅ 没有修改受保护的文件"
```

### 2. 版本依赖检查

**check-dependencies.kt：**
```kotlin
object VersionDependencyChecker {
    
    private val dependencies = mapOf(
        "v0.2" to listOf("v0.1"),
        "v0.3" to listOf("v0.1", "v0.2"),
        "v0.4" to listOf("v0.1", "v0.3"),
        "v0.5" to listOf("v0.1", "v0.2", "v0.3", "v0.4")
    )
    
    fun checkDependencies(version: String): Boolean {
        val required = dependencies[version] ?: return true
        
        for (dep in required) {
            if (!isVersionAvailable(dep)) {
                println("❌ 缺少依赖版本: $dep")
                return false
            }
        }
        
        return true
    }
}
```

## 🎯 最佳实践总结

### 1. 开发新版本时

✅ **应该做：**
- 在plans/下创建版本文件夹
- 创建独立的包/模块
- 定义清晰的接口
- 编写完整的文档
- 添加单元测试
- 使用功能开关

❌ **不应该做：**
- 直接修改旧版本的核心文件
- 删除旧版本的功能
- 修改公共接口的签名
- 跳过文档编写
- 忽略测试

### 2. 代码组织

✅ **好的做法：**
```kotlin
// 清晰的版本标识
package com.douyin.automation.v02.search

// 明确的接口定义
interface BloggerSearcher {
    suspend fun search(name: String): Result<Profile>
}

// 完整的文档
/**
 * 博主搜索器
 * @since v0.2
 * @author Team
 */
class DouyinBloggerSearcher : BloggerSearcher
```

❌ **不好的做法：**
```kotlin
// 没有版本标识
package com.douyin.automation

// 直接实现，没有接口
class Searcher {
    fun doSearch(n: String) { }  // 不清晰的命名
}
```

### 3. 文档管理

✅ **完整的版本文档：**
```
plans/v0.2/
├── requirements.md      # 需求文档
├── tasks.md            # 任务清单
├── design.md           # 设计文档
├── api.md              # API文档
├── test-report.md      # 测试报告
├── changelog.md        # 变更日志
└── lessons-learned.md  # 经验总结
```

## 🚀 实施计划

### Phase 1: 重组现有结构（立即）
- [x] 创建版本文件夹
- [ ] 移动v0.2文档到plans/v0.2/
- [ ] 创建v0.1文档（补充）
- [ ] 编写项目README

### Phase 2: 建立规范（本周）
- [ ] 编写代码规范文档
- [ ] 创建文件保护列表
- [ ] 编写版本检查脚本
- [ ] 设置Git hooks

### Phase 3: 应用到v0.3（下周）
- [ ] 按新规范开发v0.3
- [ ] 验证版本隔离效果
- [ ] 优化流程

---

**创建时间：** 2026-05-15  
**版本：** 1.0  
**状态：** 📝 草案
