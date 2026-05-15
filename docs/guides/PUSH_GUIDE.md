# 推送代码到GitHub - 安全指南

## ⚠️ 重要安全提示

**永远不要在命令行或脚本中直接使用密码！**

GitHub已经禁止使用密码进行Git操作，必须使用以下方式之一：

## 🔐 推荐方式1：使用Personal Access Token（最简单）

### 步骤1：创建Personal Access Token

1. 登录GitHub：https://github.com
2. 点击右上角头像 → Settings
3. 左侧菜单 → Developer settings
4. Personal access tokens → Tokens (classic)
5. 点击 "Generate new token" → "Generate new token (classic)"
6. 设置：
   - Note: `douyin-automation`
   - Expiration: `90 days` 或 `No expiration`
   - 勾选权限：
     - ✅ `repo` (完整仓库访问)
     - ✅ `workflow` (GitHub Actions)
7. 点击 "Generate token"
8. **复制Token（只显示一次！）**

### 步骤2：使用Token推送

```bash
# 初始化Git
cd /personal/ai_workspace/aip
git init
git add .
git commit -m "feat: v0.1 初始版本 - WebSocket通信验证"

# 先在GitHub创建仓库
# 访问 https://github.com/new
# 仓库名：douyin-automation

# 使用Token推送（替换YOUR_TOKEN为您的Token）
git remote add origin https://YOUR_TOKEN@github.com/YOUR_USERNAME/douyin-automation.git
git branch -M main
git push -u origin main
```

**Token格式示例：**
```bash
# Token格式: ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
git remote add origin https://ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx@github.com/username/douyin-automation.git
```

### 步骤3：保存Token（可选）

```bash
# 保存凭据，下次不用再输入
git config --global credential.helper store
```

---

## 🔑 推荐方式2：使用SSH密钥（最安全）

### 步骤1：生成SSH密钥

```bash
# 生成新的SSH密钥
ssh-keygen -t ed25519 -C "your_email@example.com"

# 按Enter使用默认路径
# 可以设置密码或直接Enter跳过

# 查看公钥
cat ~/.ssh/id_ed25519.pub
```

### 步骤2：添加SSH密钥到GitHub

1. 复制公钥内容
2. 登录GitHub → Settings
3. SSH and GPG keys → New SSH key
4. Title: `服务器密钥`
5. 粘贴公钥内容
6. 点击 "Add SSH key"

### 步骤3：使用SSH推送

```bash
# 初始化Git
cd /personal/ai_workspace/aip
git init
git add .
git commit -m "feat: v0.1 初始版本"

# 使用SSH URL
git remote add origin git@github.com:YOUR_USERNAME/douyin-automation.git
git branch -M main
git push -u origin main
```

---

## 🚀 快速推送脚本

我已经创建了推送脚本，您只需要：

### 1. 创建GitHub仓库

访问：https://github.com/new
- 仓库名：`douyin-automation`
- 描述：`抖音自动化营销系统`
- 选择：Public（推荐）
- **不要**勾选"Initialize this repository"

### 2. 获取Personal Access Token

按照上面"方式1"的步骤创建Token

### 3. 运行推送脚本

```bash
cd /personal/ai_workspace/aip

# 设置Token（替换为您的Token）
export GITHUB_TOKEN="YOUR_TOKEN_HERE"

# 设置用户名
export GITHUB_USER="YOUR_USERNAME"

# 运行推送脚本
bash push-to-github.sh
```

---

## 📝 手动推送步骤

如果您想手动操作：

```bash
# 1. 初始化Git
cd /personal/ai_workspace/aip
git init

# 2. 配置用户信息
git config user.name "Your Name"
git config user.email "your_email@example.com"

# 3. 添加所有文件
git add .

# 4. 首次提交
git commit -m "feat: v0.1 初始版本 - WebSocket通信验证"

# 5. 添加远程仓库（使用Token）
git remote add origin https://YOUR_TOKEN@github.com/YOUR_USERNAME/douyin-automation.git

# 6. 推送
git branch -M main
git push -u origin main
```

---

## ✅ 验证推送成功

推送成功后：

1. **查看仓库**
   - 访问 https://github.com/YOUR_USERNAME/douyin-automation
   - 应该能看到所有文件

2. **查看Actions**
   - 点击 Actions 标签
   - 应该看到自动构建任务正在运行

3. **等待构建完成**
   - 约10-15分钟
   - 构建成功后下载APK

---

## 🆘 常见问题

### Q: 推送时要求输入密码？
A: GitHub已禁用密码，必须使用Token或SSH

### Q: Token在哪里找？
A: GitHub → Settings → Developer settings → Personal access tokens

### Q: 如何保存Token避免重复输入？
```bash
git config --global credential.helper store
```

### Q: 推送失败：Permission denied
A: 检查Token权限是否包含`repo`

### Q: 推送失败：Repository not found
A: 确认仓库已创建，URL正确

### Q: 推送被拒绝：包含secrets
A: 不要在代码中包含Token，使用环境变量

---

## 🔒 安全建议

1. ✅ 使用Personal Access Token或SSH密钥
2. ✅ 定期更换Token
3. ✅ 不要在代码中保存Token
4. ✅ 不要分享Token给他人
5. ✅ 使用环境变量存储Token
6. ❌ 永远不要使用密码推送
7. ❌ 不要在公开场合展示Token
8. ❌ 不要提交包含Token的文件

---

## 📚 下一步

推送成功后：
1. 查看GitHub Actions构建状态
2. 下载构建的APK
3. 测试安装和功能
4. 继续v0.2开发

详见：[`android-app/AUTO_BUILD.md`](android-app/AUTO_BUILD.md)
