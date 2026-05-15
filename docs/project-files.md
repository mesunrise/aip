# 项目根目录文件说明

## 📁 目录结构

```
aip/
├── README.md              # 项目主文档
├── requirements.txt       # Python依赖（服务器端）
├── .gitignore            # Git忽略规则
│
├── android-app/          # Android应用
├── cloud-server/         # 云端服务器
├── docs/                 # 文档目录
├── plans/                # 开发计划
├── tasks/                # 任务配置
├── scripts/              # 脚本工具
├── apk-releases/         # APK发布
├── .github/              # GitHub配置
└── examples/             # 示例代码
```

## 📄 根目录文件

### README.md
项目主文档，包含：
- 项目概述
- 快速开始
- 目录结构
- 版本规划
- 技术栈
- API接口

### requirements.txt
服务器端Python依赖：
```
fastapi
uvicorn
websockets
pyyaml
requests
```

### .gitignore
Git忽略规则，排除：
- 构建产物
- IDE配置
- 临时文件
- 敏感信息

## 📚 子目录说明

### android-app/
Android应用源代码
- `app/` - 应用主模块
- `build.gradle.kts` - 构建配置
- `BUILD.md` - 构建说明

### cloud-server/
云端服务器源代码
- `src/` - Python源代码
- `requirements.txt` - 依赖列表

### docs/
项目文档
- `README.md` - 文档索引
- `guides/` - 使用指南
- `design/` - 设计文档
- `archive/` - 归档文档

### plans/
开发计划和版本管理
- `project-management.md` - 项目管理
- `v0.1/`, `v0.2/` - 各版本计划

### tasks/
任务配置和设计
- `automation-tasks.md` - 任务定义
- `task-system-design.md` - 系统设计

### scripts/
自动化脚本
- `auto-install-apk.sh` - APK安装
- `auto-download-apk.py` - APK下载
- `README.md` - 脚本说明

### apk-releases/
APK发布目录
- `latest.apk` - 最新版本链接
- `app-debug-*.apk` - 历史版本

### .github/
GitHub配置
- `workflows/build-apk.yml` - CI/CD配置

## 🎯 文件组织原则

1. **根目录简洁**：只保留必要的配置文件和主文档
2. **分类清晰**：按功能分类到子目录
3. **文档集中**：所有文档放在docs/
4. **代码分离**：Android和服务器代码分开
5. **工具独立**：脚本工具放在scripts/

## 📝 维护建议

- 新增文件前考虑放在哪个目录
- 定期清理不再使用的文件
- 更新文档索引
- 保持目录结构一致性

---

**最后更新：** 2026-05-15  
**维护者：** 开发团队
