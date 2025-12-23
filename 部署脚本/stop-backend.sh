#!/bin/bash
# 会计系统后端停止脚本
# 使用方法：chmod +x stop-backend.sh && ./stop-backend.sh

# 设置颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 配置变量
JAR_NAME="demo-0.0.1-SNAPSHOT.jar"
APP_DIR="/www/wwwroot/accounting-backend"
PID_FILE="${APP_DIR}/app.pid"

# 停止应用
stop_app() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            echo -e "${YELLOW}正在停止应用（PID: $PID）...${NC}"
            kill "$PID"
            
            # 等待进程结束
            for i in {1..10}; do
                if ! ps -p "$PID" > /dev/null 2>&1; then
                    echo -e "${GREEN}✓ 应用已停止${NC}"
                    rm -f "$PID_FILE"
                    return 0
                fi
                sleep 1
            done
            
            # 如果还在运行，强制杀死
            if ps -p "$PID" > /dev/null 2>&1; then
                echo -e "${YELLOW}强制停止应用...${NC}"
                kill -9 "$PID"
                sleep 1
            fi
            
            rm -f "$PID_FILE"
            echo -e "${GREEN}✓ 应用已停止${NC}"
        else
            echo -e "${YELLOW}PID文件存在但进程不存在，清理PID文件${NC}"
            rm -f "$PID_FILE"
        fi
    else
        echo -e "${YELLOW}未找到PID文件，尝试查找进程...${NC}"
    fi
    
    # 查找并停止所有相关进程
    PIDS=$(ps aux | grep "$JAR_NAME" | grep -v grep | awk '{print $2}')
    if [ -n "$PIDS" ]; then
        echo -e "${YELLOW}发现相关进程，正在停止...${NC}"
        echo "$PIDS" | xargs kill -9 2>/dev/null
        echo -e "${GREEN}✓ 所有相关进程已停止${NC}"
    else
        echo -e "${YELLOW}未发现运行中的进程${NC}"
    fi
}

# 主函数
main() {
    echo "=========================================="
    echo "  会计系统后端停止脚本"
    echo "=========================================="
    
    stop_app
    
    echo "=========================================="
}

# 执行主函数
main

