# 部署脚本使用说明

本目录包含用于部署会计系统到Linux服务器的辅助脚本和配置文件。

## 📁 文件说明

### 1. `start-backend.sh` - 后端启动脚本
用于启动后端Spring Boot应用。

**使用方法：**
```bash
# 1. 上传到服务器
# 2. 添加执行权限
chmod +x start-backend.sh

# 3. 修改脚本中的配置变量（如需要）
# JAR_NAME: jar包名称
# APP_DIR: 应用目录路径
# LOG_FILE: 日志文件路径

# 4. 执行启动
./start-backend.sh
```

**功能：**
- 自动检查Java环境
- 检查jar文件是否存在
- 停止旧进程
- 启动新进程
- 保存PID文件
- 输出启动状态

### 2. `stop-backend.sh` - 后端停止脚本
用于停止后端Spring Boot应用。

**使用方法：**
```bash
# 1. 添加执行权限
chmod +x stop-backend.sh

# 2. 执行停止
./stop-backend.sh
```

**功能：**
- 根据PID文件停止进程
- 查找并停止所有相关进程
- 清理PID文件

### 3. `application-prod.yml` - 生产环境配置文件
Spring Boot生产环境配置文件。

**使用方法：**
1. 修改配置文件中的数据库连接信息：
   - `username`: MySQL用户名
   - `password`: MySQL密码
   - `url`: 数据库连接地址（如需要）

2. 将文件放在jar包同目录

3. 启动时使用：
   ```bash
   java -jar -Dspring.profiles.active=prod demo-0.0.1-SNAPSHOT.jar
   ```

### 4. `nginx.conf.example` - Nginx配置示例
Nginx反向代理配置示例。

**使用方法：**
1. 登录宝塔面板
2. 进入 **网站** → 找到你的网站 → **设置** → **配置文件**
3. 将示例配置复制到 `server` 块中
4. 修改以下内容：
   - `server_name`: 你的域名或IP
   - `root`: 前端文件路径
   - `proxy_pass`: 后端服务地址（如果需要）
5. 保存并重启Nginx

## 🚀 快速部署流程

### 1. 准备文件
```bash
# 在本地执行
cd demo
mvn clean package -DskipTests

# 打包前端
cd ../accounting-front
npm install
npm run build
```

### 2. 上传到服务器
- 上传 `demo/target/demo-0.0.1-SNAPSHOT.jar` 到 `/www/wwwroot/accounting-backend/`
- 上传 `accounting-front/dist/` 下的所有文件到 `/www/wwwroot/accounting-front/`
- 上传 `start-backend.sh` 和 `stop-backend.sh` 到 `/www/wwwroot/accounting-backend/`
- 上传 `application-prod.yml` 到 `/www/wwwroot/accounting-backend/`

### 3. 配置数据库
```bash
# SSH连接服务器后执行
mysql -u root -p
# 创建数据库
CREATE DATABASE accounting_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
# 导入数据
mysql -u root -p accounting_system < accounting_system.sql
```

### 4. 修改配置
```bash
# 编辑生产环境配置
vi /www/wwwroot/accounting-backend/application-prod.yml
# 修改数据库用户名和密码
```

### 5. 启动后端
```bash
cd /www/wwwroot/accounting-backend
chmod +x start-backend.sh
./start-backend.sh
```

### 6. 配置Nginx
按照 `nginx.conf.example` 中的说明配置Nginx。

### 7. 验证部署
- 访问前端页面：`http://你的域名或IP`
- 检查后端日志：`tail -f /www/wwwroot/accounting-backend/app.log`

## 📝 注意事项

1. **文件权限**：确保脚本有执行权限，前端文件有读取权限
2. **端口开放**：确保防火墙已开放80、8080端口
3. **Java版本**：确保服务器已安装JDK 8+
4. **MySQL版本**：确保MySQL版本与驱动匹配（5.7用`com.mysql.jdbc.Driver`，8.0+用`com.mysql.cj.jdbc.Driver`）
5. **日志查看**：定期查看日志文件，及时发现问题

## 🔧 常见问题

### Q: 脚本执行权限不足？
A: 使用 `chmod +x 脚本名.sh` 添加执行权限

### Q: Java命令找不到？
A: 检查Java是否安装，或设置JAVA_HOME环境变量

### Q: 端口被占用？
A: 检查端口占用：`netstat -tlnp | grep 8080`，或修改配置文件中的端口

### Q: 数据库连接失败？
A: 检查数据库服务是否启动，用户名密码是否正确，数据库是否已创建

## 📞 技术支持

如遇到问题，请查看：
- 后端日志：`/www/wwwroot/accounting-backend/app.log`
- Nginx日志：`/www/wwwlogs/accounting-error.log`
- 系统日志：`journalctl -u nginx` 或 `journalctl -u mysql`

