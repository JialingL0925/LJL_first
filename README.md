小微企业会计系统

一个基于 Spring Boot + Vue 3 开发的企业级会计管理系统，支持采购、销售、应收应付、凭证过账等核心财务功能。

本项目为系统分析与设计课程的作业项目。

1. 项目简介

本项目是一个面向小微企业的会计管理系统，提供完整的财务业务流程管理，包括：

1.1 采购管理：采购订单、应付单、付款单管理
1.2 销售管理：销售订单、应收单、收款单管理
1.3 凭证管理：会计分录录入、审核、过账
1.4 报表统计：资产负债表、利润表、现金流量表
1.5 基础数据：会计科目、客户、供应商、员工、项目管理

2. 技术栈

2.1 后端技术
框架：Spring Boot 2.7.15
数据库：MySQL 5.7+
ORM：MyBatis-Plus 3.5.3.1
安全：Spring Security + JWT
工具：Maven

2.2 前端技术
框架：Vue 3.4.21
UI组件：Element Plus 2.6.1
构建工具：Vite 5.2.0
图表：ECharts 5.6.0
HTTP客户端：Axios 1.6.8
路由：Vue Router 4.3.0

3. 项目结构

```
accounting/
├── demo/                          # 后端项目（Spring Boot）
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/example/demo/
│   │   │   │       ├── controller/    # 控制器层
│   │   │   │       ├── service/       # 服务层
│   │   │   │       ├── mapper/        # 数据访问层
│   │   │   │       ├── model/         # 实体类、DTO、VO
│   │   │   │       ├── config/        # 配置类
│   │   │   │       └── util/          # 工具类
│   │   │   └── resources/
│   │   │       ├── application.yml    # 配置文件
│   │   │       └── mapper/            # MyBatis XML映射文件
│   │   └── test/                      # 测试代码
│   └── pom.xml                       # Maven依赖配置
│
├── accounting-front/                # 前端项目（Vue 3）
│   ├── src/
│   │   ├── api/                      # API接口定义
│   │   ├── views/                    # 页面组件
│   │   ├── router/                   # 路由配置
│   │   └── utils/                    # 工具函数
│   ├── package.json                  # 依赖配置
│   └── vite.config.js               # Vite配置
│
├── accounting_system.sql            # 数据库脚本
└── README.md                        # 项目说明文档
```

4. 快速开始

4.1 环境要求

JDK：1.8+
Node.js：16+
MySQL：5.7+
Maven：3.6+

4.2 数据库配置

步骤1：创建数据库
```sql
CREATE DATABASE accounting_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

步骤2：导入数据库脚本
```bash
mysql -u root -p accounting_system < accounting_system.sql
```

步骤3：修改后端配置文件 demo/src/main/resources/application.yml
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/accounting_system?useUnicode=true&characterEncoding=utf8&useSSL=false
    username: 你的数据库用户名
    password: 你的数据库密码
```

4.3 后端启动

```bash
# 进入后端目录
cd demo

# 使用 Maven 打包
mvn clean package

# 运行项目
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

后端服务默认运行在：http://localhost:8080

4.4 前端启动

```bash
# 进入前端目录
cd accounting-front

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务默认运行在：http://localhost:5173

4.5 生产环境构建

```bash
# 前端构建
cd accounting-front
npm run build

# 构建产物在 dist/ 目录
```

5. 默认账号

系统预置了以下测试账号：

| 角色 | 手机号 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | 13800138000 | 123456 | 系统管理员 |
| 会计 | 13700137003 | 123456 | 会计人员 |
| 采购 | 13900139002 | 123456 | 采购人员 |
| 出纳 | 13900139003 | 123456 | 出纳人员 |
| 销售 | 13900139004 | 123456 | 销售人员 |

6. 核心功能

6.1 会计科目管理
支持多级科目结构
科目启用/停用管理
科目类型分类（资产/负债/所有者权益/成本/损益）

6.2 采购管理
采购订单创建与审核
自动生成应付单
付款单录入与核销

6.3 销售管理
销售订单创建与审核
自动生成应收单
收款单录入与核销

6.4 凭证管理
手工凭证录入
业务单据自动生成凭证
凭证过账功能
凭证查询与统计

6.5 财务报表
资产负债表
利润表
现金流量表
项目报表

6.6 基础数据管理
客户管理
供应商管理
员工管理
项目管理

7. 权限管理

系统支持基于角色的权限控制（RBAC）：

7.1 ADMIN：系统管理员，拥有所有权限
7.2 ACCOUNTANT：会计，负责凭证和报表管理
7.3 PURCHASER：采购，负责采购订单管理
7.4 CASHIER：出纳，负责收付款管理
7.5 SALES：销售，负责销售订单管理

8. API 文档

后端提供 RESTful API 接口，主要接口包括：

8.1 /api/login 用户登录
8.2 /api/employee/** 员工管理
8.3 /api/accounting-subject/** 会计科目管理
8.4 /api/purchase-order/** 采购订单管理
8.5 /api/sales-order/** 销售订单管理
8.6 /api/journal-entry/** 凭证管理
8.7 /api/report/** 报表查询

详细接口文档请参考代码中的 Controller 类。

9. 数据库设计

系统包含以下主要数据表：

9.1 accounting_subject 会计科目表
9.2 employee 员工表
9.3 role 角色表
9.4 purchase_order 采购订单表
9.5 sales_order 销售订单表
9.6 ap_payable 应付单表
9.7 ar_receivable 应收单表
9.8 ap_payment 付款单表
9.9 ar_payment 收款单表
9.10 journal_entry 凭证表
9.11 project 项目表
9.12 sys_customer 客户表
9.13 sys_supplier 供应商表

详细表结构请查看 accounting_system.sql 文件。

12. 作者

Jialing Liu - Project Developer

本项目为系统分析与设计课程作业。


如果这个项目对你有帮助，欢迎 Star！
