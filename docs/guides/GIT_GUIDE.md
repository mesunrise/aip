# Git使用指南

## 初始化Git仓库

```bash
cd /personal/ai_workspace/aip

# 初始化Git
git init

# 添加所有文件
git add .

# 首次提交
git commit -m "feat: v0.1 初始版本 - WebSocket通信验证"
```

## 创建GitHub仓库

### 方法1：通过GitHub网站

1. 访问 https://github.com/new
2. 仓库名：`douyin-automation`
3. 描述：`抖音自动化营销系统 - Android App + 云端控制`
4. 选择：Public（公开）或 Private（私有）
5. 不要勾选"Initialize this repository with a README"
6. 点击"Create repository"

### 方法2：使用GitHub CLI

```bash
# 安装GitHub CLI
# https://cli.github.com/

# 登录
gh auth login

# 创建仓库
gh repo create douyin-automation --public --source=. --remote=origin
```

## 关联远程仓库

```bash
# 添加远程仓库
git remote add origin https://github.com/你的用户名/douyin-automation.git

# 或使用SSH
git remote add origin git@github.com:你的用户名/douyin-automation.git

# 设置主分支
git branch -M main

# 首次推送
git push -u origin main
```

## 日常开发流程

### 1. 修改代码

```bash
# 查看状态
git status

# 查看修改
git diff
```

### 2. 提交更改

```bash
# 添加文件
git add .

# 或添加特定文件
git add android-app/app/src/main/java/com/douyin/automation/MainActivity.kt

# 提交
git commit -m "feat: 添加新功能"
```

### 3. 推送到GitHub

```bash
# 推送
git push

# 首次推送新分支
git push -u origin feature-branch
```

## 提交信息规范

```bash
# 新功能
git commit -m "feat: 实现无障碍服务"

# 修复bug
git commit -m "fix: 修复连接断开问题"

# 文档更新
git commit -m "docs: 更新README"

# 代码重构
git commit -m "refactor: 重构WebSocket客户端"

# 性能优化
git commit -m "perf: 优化内存使用"

# 测试
git commit -m "test: 添加单元测试"

# 构建相关
git commit -m "build: 更新依赖版本"
```

## 分支管理

### 创建功能分支

```bash
# 创建并切换到新分支
git checkout -b feature/accessibility-service

# 开发完成后推送
git push -u origin feature/accessibility-service

# 在GitHub创建Pull Request
```

### 合并分支

```bash
# 切换到主分支
git checkout main

# 拉取最新代码
git pull

# 合并功能分支
git merge feature/accessibility-service

# 推送
git push
```

## 版本标签

### 创建标签

```bash
# 创建标签
git tag v0.1.0

# 推送标签
git push origin v0.1.0

# 推送所有标签
git push --tags
```

### 查看标签

```bash
# 列出所有标签
git tag

# 查看标签详情
git show v0.1.0
```

## 常用命令

```bash
# 查看提交历史
git log --oneline

# 查看远程仓库
git remote -v

# 拉取最新代码
git pull

# 撤销修改
git checkout -- 文件名

# 撤销暂存
git reset HEAD 文件名

# 查看分支
git branch -a

# 删除分支
git branch -d feature-branch
```

## .gitignore配置

项目已包含 `.gitignore` 文件，忽略以下内容：

```
# Android
*.apk
*.ap_
*.dex
.gradle/
build/
local.properties

# Python
__pycache__/
*.pyc
*.pyo
.env
venv/

# IDE
.idea/
.vscode/
*.swp

# 其他
.DS_Store
*.log
```

## 推送到GitHub后

1. **查看Actions**
   - 访问仓库的Actions标签
   - 查看构建状态

2. **下载APK**
   - 构建完成后
   - 在Artifacts区域下载

3. **创建Release**
   - 推送标签后自动创建
   - 或手动创建Release

## 故障排查

### 推送失败

```bash
# 拉取最新代码
git pull --rebase

# 解决冲突后
git push
```

### 忘记添加文件

```bash
# 修改最后一次提交
git add 遗漏的文件
git commit --amend --no-edit
git push --force
```

### 撤销推送

```bash
# 回退到上一个提交
git reset --hard HEAD~1

# 强制推送
git push --force
```

## 下一步

推送代码后：
1. 查看GitHub Actions构建状态
2. 下载构建的APK
3. 测试安装和功能
4. 继续v0.2开发

详见：[`android-app/AUTO_BUILD.md`](android-app/AUTO_BUILD.md)
