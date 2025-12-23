#!/bin/bash
# 会计系统后端启动脚本
# 使用方法：chmod +x start-backend.sh && ./start-backend.sh

# 设置颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 配置变量（根据实际情况修改）
JAR_NAME="demo-0.0.1-SNAPSHOT.jar"
APP_DIR="/www/wwwroot/accounting-backend"
LOG_FILE="${APP_DIR}/app.log"
PID_FILE="${APP_DIR}/app.pid"

# 检查Java环境
check_java() {
    if ! command -v java &> /dev/null; then
        echo -e "${RED}错误：未找到Java环境，请先安装JDK 8+${NC}"
        exit 1
    fi
    
    # 获取Java版本号（如：1.8.0_412 或 11.0.1）
    JAVA_VERSION_STRING=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    
    # 提取主版本号
    # Java 8: 1.8.x -> 提取第二个数字 8
    # Java 9+: 9.x, 10.x, 11.x -> 提取第一个数字
    if echo "$JAVA_VERSION_STRING" | grep -q "^1\."; then
        # Java 8格式：1.8.x，提取第二个数字
        JAVA_MAJOR_VERSION=$(echo "$JAVA_VERSION_STRING" | cut -d'.' -f2)
    else
        # Java 9+格式：9.x, 10.x, 11.x，提取第一个数字
        JAVA_MAJOR_VERSION=$(echo "$JAVA_VERSION_STRING" | cut -d'.' -f1)
    fi
    
    # 检查版本是否 >= 8
    if [ -z "$JAVA_MAJOR_VERSION" ] || [ "$JAVA_MAJOR_VERSION" -lt 8 ]; then
        echo -e "${RED}错误：Java版本过低，需要JDK 8+（当前版本：$JAVA_VERSION_STRING）${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Java环境检查通过（版本：$JAVA_VERSION_STRING）${NC}"
}

# 检查jar文件是否存在
check_jar() {
    if [ ! -f "${APP_DIR}/${JAR_NAME}" ]; then
        echo -e "${RED}错误：未找到jar文件：${APP_DIR}/${JAR_NAME}${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Jar文件检查通过${NC}"
}

# 停止旧进程
stop_old_process() {
    if [ -f "$PID_FILE" ]; then
        OLD_PID=$(cat "$PID_FILE")
        if ps -p "$OLD_PID" > /dev/null 2>&1; then
            echo -e "${YELLOW}正在停止旧进程（PID: $OLD_PID）...${NC}"
            kill "$OLD_PID"
            sleep 2
            
            # 如果还在运行，强制杀死
            if ps -p "$OLD_PID" > /dev/null 2>&1; then
                echo -e "${YELLOW}强制停止进程...${NC}"
                kill -9 "$OLD_PID"
            fi
        fi
        rm -f "$PID_FILE"
    fi
    
    # 查找并停止所有相关进程
    PIDS=$(ps aux | grep "$JAR_NAME" | grep -v grep | awk '{print $2}')
    if [ -n "$PIDS" ]; then
        echo -e "${YELLOW}发现残留进程，正在清理...${NC}"
        echo "$PIDS" | xargs kill -9 2>/dev/null
    fi
}

# 启动应用
start_app() {
    echo -e "${YELLOW}正在启动应用...${NC}"
    
    cd "$APP_DIR" || exit 1
    
    # 启动应用
    nohup java -jar \
        -Dspring.profiles.active=prod \
        -Xms512m \
        -Xmx1024m \
        "$JAR_NAME" > "$LOG_FILE" 2>&1 &
    
    # 保存PID
    echo $! > "$PID_FILE"
    
    sleep 3
    
    # 检查是否启动成功
    if ps -p $(cat "$PID_FILE") > /dev/null 2>&1; then
        echo -e "${GREEN}✓ 应用启动成功！${NC}"
        echo -e "${GREEN}  PID: $(cat $PID_FILE)${NC}"
        echo -e "${GREEN}  日志文件: $LOG_FILE${NC}"
        echo -e "${YELLOW}查看日志: tail -f $LOG_FILE${NC}"
    else
        echo -e "${RED}✗ 应用启动失败，请查看日志: $LOG_FILE${NC}"
        exit 1
    fi
}

# 主函数
main() {
    echo "=========================================="
    echo "  会计系统后端启动脚本"
    echo "=========================================="
    
    check_java
    check_jar
    stop_old_process
    start_app
    
    echo "=========================================="
}

# 执行主函数
main

