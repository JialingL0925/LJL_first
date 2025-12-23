# 将项目代码上传到 GitHub 的完整指南

## 前置准备

### 1. 安装 Git（如果尚未安装）

如果您的系统还没有安装 Git，请先下载安装：
- 访问：https://git-scm.com/download/win
- 下载并安装 Git for Windows
- 安装完成后，重启命令行工具

### 2. 配置 Git 用户信息（首次使用需要）

打开 PowerShell 或命令提示符，执行：

```bash
git config --global user.name "您的用户名"
git config --global user.email "您的邮箱"
```

### 3. 创建 GitHub 仓库

1. 登录 GitHub（https://github.com）
2. 点击右上角的 "+" 号，选择 "New repository"
3. 填写仓库名称（例如：accounting-system）
4. 选择公开（Public）或私有（Private）
5. **不要**勾选 "Initialize this repository with a README"（因为本地已有代码）
6. 点击 "Create repository"

## 上传代码步骤

### 步骤 1：初始化 Git 仓库（如果尚未初始化）

在项目根目录（`d:\小项目\accounting`）打开 PowerShell，执行：

```bash
cd "d:\小项目\accounting"
git init
```

### 步骤 2：创建 .gitignore 文件

在项目根目录创建 `.gitignore` 文件，排除不需要上传的文件：

```gitignore
# 依赖目录
node_modules/
target/
dist/

# 编译文件
*.class
*.jar
*.war
*.ear

# IDE 文件
.idea/
.vscode/
*.iml
*.ipr
*.iws

# 系统文件
.DS_Store
Thumbs.db
*.swp
*.swo
*~

# 日志文件
*.log
logs/

# 临时文件
*.tmp
*.temp

# 环境配置文件（如果包含敏感信息）
.env
.env.local

# Maven
.mvn/
mvnw
mvnw.cmd

# 打包文件
*.jar
*.war
*.ear
```

### 步骤 3：添加文件到暂存区

```bash
git add .
```

如果只想添加特定文件：

```bash
git add src/
git add pom.xml
git add package.json
# 等等...
```

### 步骤 4：提交代码

```bash
git commit -m "初始提交：会计系统项目"
```

### 步骤 5：添加远程仓库

将您的 GitHub 仓库地址添加为远程仓库（替换为您的实际仓库地址）：

```bash
git remote add origin https://github.com/您的用户名/仓库名.git
```

例如：
```bash
git remote add origin https://github.com/username/accounting-system.git
```

### 步骤 6：推送代码到 GitHub

```bash
git branch -M main
git push -u origin main
```

如果您的 GitHub 仓库默认分支是 `master`，则使用：

```bash
git branch -M master
git push -u origin master
```

## 常见问题处理

### 问题 1：GitHub 认证失败

如果推送时提示需要认证，您需要：

1. **使用 Personal Access Token（推荐）**：
   - 访问：https://github.com/settings/tokens
   - 点击 "Generate new token" -> "Generate new token (classic)"
   - 选择权限：至少勾选 `repo`
   - 生成后复制 token
   - 推送时，用户名输入您的 GitHub 用户名，密码输入 token

2. **或者使用 SSH 密钥**：
   ```bash
   # 生成 SSH 密钥
   ssh-keygen -t ed25519 -C "your_email@example.com"
   
   # 复制公钥内容
   cat ~/.ssh/id_ed25519.pub
   
   # 将公钥添加到 GitHub：Settings -> SSH and GPG keys -> New SSH key
   
   # 使用 SSH 地址添加远程仓库
   git remote set-url origin git@github.com:您的用户名/仓库名.git
   ```

### 问题 2：远程仓库已存在文件

如果 GitHub 仓库已经初始化了 README 等文件，需要先拉取：

```bash
git pull origin main --allow-unrelated-histories
# 解决可能的冲突后
git push -u origin main
```

### 问题 3：路径包含中文导致问题

如果遇到路径问题，可以尝试使用短路径：

```bash
# 查看短路径
cmd /c dir /x
```

## 后续更新代码

当您修改代码后，需要更新到 GitHub：

```bash
# 1. 查看修改的文件
git status

# 2. 添加修改的文件
git add .

# 3. 提交修改
git commit -m "描述您的修改内容"

# 4. 推送到 GitHub
git push
```

## 快速命令总结

```bash
# 初始化仓库（首次）
git init
git add .
git commit -m "初始提交"
git remote add origin https://github.com/您的用户名/仓库名.git
git branch -M main
git push -u origin main

# 后续更新
git add .
git commit -m "更新说明"
git push
```

## 注意事项

1. **不要上传敏感信息**：确保 `.gitignore` 文件排除了包含密码、API 密钥等敏感信息的文件
2. **不要上传大文件**：GitHub 对单个文件大小有限制（100MB），大文件建议使用 Git LFS
3. **定期提交**：养成经常提交和推送的习惯，避免代码丢失
4. **提交信息要清晰**：使用有意义的提交信息，方便后续查看历史


