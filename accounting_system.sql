/*
MySQL Data Transfer
Source Host: localhost
Source Database: accounting_system
Target Host: localhost
Target Database: accounting_system
Date: 2025/12/23 13:28:10
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for accounting_subject
-- ----------------------------
CREATE TABLE `accounting_subject` (
  `subject_id` bigint(20) NOT NULL COMMENT '科目ID（主键）',
  `subject_code` varchar(50) NOT NULL COMMENT '科目编码',
  `subject_name` varchar(255) NOT NULL COMMENT '科目名称',
  `subject_level` int(11) NOT NULL COMMENT '科目级别（1=一级/2=二级/3=三级等）',
  `parent_subject_id` bigint(20) DEFAULT NULL COMMENT '父科目ID（外键关联自身subject_id）',
  `subject_type` varchar(50) NOT NULL COMMENT '科目类型（资产/负债/所有者权益/成本/损益等）',
  `status` varchar(20) NOT NULL COMMENT '状态（启用/停用）',
  `deactivation_reason` varchar(500) DEFAULT NULL COMMENT '停用原因',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`subject_id`),
  UNIQUE KEY `uk_subject_code` (`subject_code`),
  KEY `idx_parent_subject_id` (`parent_subject_id`),
  KEY `idx_status` (`status`),
  KEY `idx_subject_type` (`subject_type`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='会计科目表';

-- ----------------------------
-- Table structure for ap_payable
-- ----------------------------
CREATE TABLE `ap_payable` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '应付单ID',
  `payable_no` varchar(50) NOT NULL COMMENT '应付单编号（AP+日期+序号）',
  `order_id` bigint(20) NOT NULL COMMENT '关联采购订单ID',
  `supplier_id` bigint(20) NOT NULL COMMENT '供应商ID',
  `supplier_name` varchar(200) NOT NULL COMMENT '供应商名称',
  `total_amount` decimal(18,2) NOT NULL COMMENT '应付总金额（价税合计）',
  `status` varchar(20) NOT NULL DEFAULT '待确认' COMMENT '状态：待确认/已确认/已核销/已取消',
  `create_user_id` bigint(20) DEFAULT '0' COMMENT '创建人ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payable_no` (`payable_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_status` (`status`)
) ENGINE=MyISAM AUTO_INCREMENT=18 DEFAULT CHARSET=utf8 COMMENT='应付单主表';

-- ----------------------------
-- Table structure for ap_payment
-- ----------------------------
CREATE TABLE `ap_payment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '付款单ID',
  `payment_no` varchar(50) NOT NULL COMMENT '付款单编号（PAY+日期+序号）',
  `payable_id` bigint(20) NOT NULL COMMENT '关联应付单ID',
  `supplier_id` bigint(20) NOT NULL COMMENT '供应商ID',
  `supplier_name` varchar(200) NOT NULL COMMENT '供应商名称',
  `payment_amount` decimal(18,2) NOT NULL COMMENT '付款金额',
  `payment_date` datetime NOT NULL COMMENT '付款日期',
  `payment_method` varchar(20) NOT NULL COMMENT '付款方式',
  `status` varchar(20) NOT NULL DEFAULT '待确认' COMMENT '状态：待确认/已确认/已取消',
  `create_user_id` bigint(20) DEFAULT '0' COMMENT '创建人ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_payable_id` (`payable_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_status` (`status`)
) ENGINE=MyISAM AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COMMENT='付款单主表';

-- ----------------------------
-- Table structure for ar_payment
-- ----------------------------
CREATE TABLE `ar_payment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '收款单ID',
  `payment_no` varchar(50) NOT NULL COMMENT '收款单编号（REC+日期+序号）',
  `receivable_id` bigint(20) NOT NULL COMMENT '关联应收单ID',
  `customer_id` bigint(20) NOT NULL COMMENT '客户ID',
  `customer_name` varchar(200) NOT NULL COMMENT '客户名称',
  `payment_amount` decimal(18,2) NOT NULL COMMENT '收款金额',
  `payment_date` datetime NOT NULL COMMENT '收款日期',
  `payment_method` varchar(20) NOT NULL COMMENT '收款方式',
  `status` varchar(20) NOT NULL DEFAULT '待确认' COMMENT '状态：待确认/已确认/已取消',
  `create_user_id` bigint(20) DEFAULT '0' COMMENT '创建人ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_receivable_id` (`receivable_id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_status` (`status`)
) ENGINE=MyISAM AUTO_INCREMENT=11 DEFAULT CHARSET=utf8 COMMENT='收款单主表';

-- ----------------------------
-- Table structure for ar_receivable
-- ----------------------------
CREATE TABLE `ar_receivable` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '应收单ID',
  `receivable_no` varchar(50) NOT NULL COMMENT '应收单编号（AR+日期+序号）',
  `order_id` bigint(20) NOT NULL COMMENT '关联销售订单ID',
  `customer_id` bigint(20) NOT NULL COMMENT '客户ID',
  `customer_name` varchar(200) NOT NULL COMMENT '客户名称',
  `total_amount` decimal(18,2) NOT NULL COMMENT '应收总金额（价税合计）',
  `status` varchar(20) NOT NULL DEFAULT '待确认' COMMENT '状态：待确认/已确认/已核销/已取消',
  `create_user_id` bigint(20) DEFAULT '0' COMMENT '创建人ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_receivable_no` (`receivable_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_status` (`status`)
) ENGINE=MyISAM AUTO_INCREMENT=11 DEFAULT CHARSET=utf8 COMMENT='应收款单主表';

-- ----------------------------
-- Table structure for biz_account_mapping
-- ----------------------------
CREATE TABLE `biz_account_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `biz_type` varchar(20) NOT NULL,
  `biz_sub_type` varchar(50) NOT NULL,
  `debit_code` varchar(50) NOT NULL,
  `credit_code` varchar(50) NOT NULL,
  `remark` varchar(500) DEFAULT '',
  `create_time` datetime NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_type_sub` (`biz_type`,`biz_sub_type`)
) ENGINE=MyISAM AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for employee
-- ----------------------------
CREATE TABLE `employee` (
  `employee_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '员工ID',
  `name` varchar(255) NOT NULL COMMENT '姓名',
  `phone` varchar(50) NOT NULL COMMENT '手机号（登录账号）',
  `password` varchar(255) DEFAULT NULL COMMENT '密码（加密后）',
  `status` varchar(20) NOT NULL COMMENT '状态（在职/停用）',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`employee_id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `fk_emp_role` (`role_id`)
) ENGINE=MyISAM AUTO_INCREMENT=8 DEFAULT CHARSET=utf8 COMMENT='员工表';

-- ----------------------------
-- Table structure for journal_entry
-- ----------------------------
CREATE TABLE `journal_entry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `source_type` varchar(20) NOT NULL,
  `source_id` bigint(20) DEFAULT NULL,
  `debit_code` varchar(50) NOT NULL,
  `debit_name` varchar(255) NOT NULL,
  `credit_code` varchar(50) NOT NULL,
  `credit_name` varchar(255) NOT NULL,
  `amount` decimal(18,2) NOT NULL,
  `tax_amount` decimal(18,2) DEFAULT '0.00',
  `status` varchar(20) NOT NULL DEFAULT '已生效',
  `remark` varchar(500) DEFAULT '',
  `create_user_id` bigint(20) DEFAULT '0',
  `post_user_id` bigint(20) DEFAULT NULL COMMENT '过账人ID',
  `post_time` datetime DEFAULT NULL COMMENT '过账时间',
  `create_time` datetime NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_source` (`source_type`,`source_id`),
  KEY `idx_debit` (`debit_code`),
  KEY `idx_credit` (`credit_code`),
  KEY `idx_post_time` (`post_time`),
  KEY `idx_status` (`status`)
) ENGINE=MyISAM AUTO_INCREMENT=56 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for project
-- ----------------------------
CREATE TABLE `project` (
  `project_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `project_code` varchar(50) NOT NULL COMMENT '项目编号（唯一）',
  `project_name` varchar(200) NOT NULL COMMENT '项目名称',
  `project_manager` varchar(100) DEFAULT NULL COMMENT '项目经理/负责人',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `budget` decimal(18,2) DEFAULT '0.00' COMMENT '预算金额',
  `status` varchar(20) NOT NULL DEFAULT '进行中' COMMENT '状态（进行中/已完成/已暂停/已取消）',
  `description` varchar(500) DEFAULT NULL COMMENT '项目描述',
  `create_user_id` bigint(20) DEFAULT '0' COMMENT '创建人ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`project_id`),
  UNIQUE KEY `uk_project_code` (`project_code`),
  KEY `idx_status` (`status`),
  KEY `idx_create_user` (`create_user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='项目表';

-- ----------------------------
-- Table structure for purchase_order
-- ----------------------------
CREATE TABLE `purchase_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) NOT NULL,
  `supplier_id` bigint(20) NOT NULL,
  `supplier_name` varchar(200) NOT NULL,
  `biz_sub_type` varchar(50) NOT NULL,
  `total_amount` decimal(18,2) DEFAULT '0.00' COMMENT '订单总金额（订单项汇总）',
  `status` varchar(20) NOT NULL DEFAULT '待审核',
  `create_user_id` bigint(20) DEFAULT '0',
  `create_time` datetime NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `project_id` bigint(20) DEFAULT NULL COMMENT '关联项目ID',
  `project_name` varchar(200) DEFAULT NULL COMMENT '项目名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_supplier` (`supplier_id`),
  KEY `idx_status` (`status`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=MyISAM AUTO_INCREMENT=26 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for purchase_order_line
-- ----------------------------
CREATE TABLE `purchase_order_line` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
  `order_id` bigint(20) NOT NULL COMMENT '关联采购订单ID',
  `material_code` varchar(50) NOT NULL COMMENT '物料编码',
  `material_name` varchar(255) NOT NULL COMMENT '物料名称',
  `spec` varchar(100) DEFAULT '' COMMENT '规格型号',
  `unit` varchar(20) NOT NULL COMMENT '计量单位',
  `quantity` decimal(18,2) NOT NULL COMMENT '采购数量',
  `price` decimal(18,2) NOT NULL COMMENT '不含税单价',
  `amount` decimal(18,2) DEFAULT '0.00' COMMENT '不含税金额（数量×单价）',
  `tax_rate` decimal(4,2) DEFAULT '0.13' COMMENT '税率',
  `tax_amount` decimal(18,2) DEFAULT '0.00' COMMENT '税额（金额×税率）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=MyISAM AUTO_INCREMENT=23 DEFAULT CHARSET=utf8 COMMENT='采购订单项表';

-- ----------------------------
-- Table structure for role
-- ----------------------------
CREATE TABLE `role` (
  `role_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(255) NOT NULL COMMENT '角色名称',
  PRIMARY KEY (`role_id`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COMMENT='角色表';

-- ----------------------------
-- Table structure for sales_order
-- ----------------------------
CREATE TABLE `sales_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) NOT NULL,
  `customer_id` bigint(20) NOT NULL,
  `customer_name` varchar(200) NOT NULL,
  `biz_sub_type` varchar(50) NOT NULL,
  `total_amount` decimal(18,2) DEFAULT '0.00' COMMENT '订单总金额（订单项汇总）',
  `status` varchar(20) NOT NULL DEFAULT '待审核',
  `create_user_id` bigint(20) DEFAULT '0',
  `create_time` datetime NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `project_id` bigint(20) DEFAULT NULL COMMENT '关联项目ID',
  `project_name` varchar(200) DEFAULT NULL COMMENT '项目名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_customer` (`customer_id`),
  KEY `idx_status` (`status`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=MyISAM AUTO_INCREMENT=25 DEFAULT CHARSET=utf8 COMMENT='销售订单主表';

-- ----------------------------
-- Table structure for sales_order_line
-- ----------------------------
CREATE TABLE `sales_order_line` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
  `order_id` bigint(20) NOT NULL COMMENT '关联销售订单ID',
  `product_code` varchar(50) NOT NULL COMMENT '产品编码',
  `product_name` varchar(255) NOT NULL COMMENT '产品名称',
  `spec` varchar(100) DEFAULT '' COMMENT '规格型号',
  `unit` varchar(20) NOT NULL COMMENT '计量单位',
  `quantity` decimal(18,2) NOT NULL COMMENT '销售数量',
  `price` decimal(18,2) NOT NULL COMMENT '不含税单价',
  `amount` decimal(18,2) DEFAULT '0.00' COMMENT '不含税金额（数量×单价）',
  `tax_rate` decimal(4,2) DEFAULT '0.13' COMMENT '税率',
  `tax_amount` decimal(18,2) DEFAULT '0.00' COMMENT '税额（金额×税率）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=MyISAM AUTO_INCREMENT=24 DEFAULT CHARSET=utf8 COMMENT='销售订单项表';

-- ----------------------------
-- Table structure for sys_customer
-- ----------------------------
CREATE TABLE `sys_customer` (
  `customerId` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `phone` varchar(50) DEFAULT NULL COMMENT '电话',
  `customerName` varchar(255) NOT NULL COMMENT '客户名称',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `address` varchar(500) DEFAULT NULL COMMENT '地址',
  `registrationDate` datetime DEFAULT NULL COMMENT '注册日期',
  `remarks` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` varchar(20) NOT NULL COMMENT '状态（启用/停用）',
  `createTime` datetime DEFAULT NULL COMMENT '创建时间',
  `updateTime` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`customerId`)
) ENGINE=MyISAM AUTO_INCREMENT=11 DEFAULT CHARSET=utf8 COMMENT='客户表';

-- ----------------------------
-- Table structure for sys_supplier
-- ----------------------------
CREATE TABLE `sys_supplier` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '供应商ID（主键）',
  `supplier_name` varchar(200) NOT NULL COMMENT '供应商全称（如：XX商贸有限公司）',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `address` varchar(500) DEFAULT NULL COMMENT '详细地址',
  `business_license` varchar(200) DEFAULT NULL COMMENT '营业执照编号',
  `bank_name` varchar(100) DEFAULT NULL COMMENT '开户银行',
  `bank_account` varchar(50) DEFAULT NULL COMMENT '银行账号',
  `tax_id` varchar(50) DEFAULT NULL COMMENT '纳税人识别号',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态（1-正常，2-停用）',
  `create_user_id` bigint(20) DEFAULT '0' COMMENT '创建人ID',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_supplier_name` (`supplier_name`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_create_user` (`create_user_id`) USING BTREE
) ENGINE=MyISAM AUTO_INCREMENT=13 DEFAULT CHARSET=utf8 COMMENT='供应商管理表';

-- ----------------------------
-- Records 
-- ----------------------------
INSERT INTO `accounting_subject` VALUES ('1', '1001', '库存现金', '1', '0', '资产', '启用', null, '2025-12-06 20:57:15', '2025-12-06 20:57:15');
INSERT INTO `accounting_subject` VALUES ('1997310346336071681', '1002', '银行存款', '1', '0', '资产', '启用', '', '2025-12-06 22:21:09', '2025-12-06 22:21:09');
INSERT INTO `accounting_subject` VALUES ('1997310346336071682', '1403', '原材料', '1', '0', '资产', '启用', null, '2025-12-08 17:00:00', '2025-12-08 17:00:00');
INSERT INTO `accounting_subject` VALUES ('1997310346336071683', '2202', '应付账款', '1', '0', '负债', '启用', null, '2025-12-08 17:00:00', '2025-12-08 17:00:00');
INSERT INTO `accounting_subject` VALUES ('1997310346336071684', '22210101', '应交税费-应交增值税-进项税额', '2', '2221', '负债', '启用', null, '2025-12-08 17:00:00', '2025-12-08 17:00:00');
INSERT INTO `accounting_subject` VALUES ('1997310346336071685', '6602', '管理费用', '1', '0', '损益', '启用', null, '2025-12-08 17:00:00', '2025-12-08 17:00:00');
INSERT INTO `accounting_subject` VALUES ('1997310346336071686', '1601', '固定资产', '1', '0', '资产', '启用', null, '2025-12-08 17:00:00', '2025-12-08 17:00:00');
INSERT INTO `accounting_subject` VALUES ('1997310346336071687', '2221', '应交税费', '1', '0', '负债', '启用', null, '2025-12-08 17:00:00', '2025-12-08 17:00:00');
INSERT INTO `accounting_subject` VALUES ('1997310346336071688', '1122', '应收账款', '1', '0', '资产', '启用', null, '2025-12-10 18:07:50', '2025-12-10 18:07:50');
INSERT INTO `accounting_subject` VALUES ('1997310346336071689', '6001', '主营业务收入', '1', '0', '损益', '启用', null, '2025-12-10 18:07:50', '2025-12-10 18:07:50');
INSERT INTO `accounting_subject` VALUES ('1997310346336071690', '6051', '其他业务收入', '1', '0', '损益', '启用', null, '2025-12-10 18:07:50', '2025-12-10 18:07:50');
INSERT INTO `accounting_subject` VALUES ('1997310346336071691', '6111', '资产处置收益', '1', '0', '损益', '启用', null, '2025-12-10 18:07:50', '2025-12-10 18:07:50');
INSERT INTO `accounting_subject` VALUES ('1997310346336071692', '22210102', '应交税费-应交增值税-销项税额', '2', '2221', '负债', '启用', null, '2025-12-10 18:07:50', '2025-12-10 18:07:50');
INSERT INTO `accounting_subject` VALUES ('1997310346336071693', '1221', '其他应收款', '1', '0', '资产', '启用', null, '2025-12-15 19:12:40', '2025-12-15 19:12:40');
INSERT INTO `accounting_subject` VALUES ('1997310346336071694', '1405', '库存商品', '1', '0', '资产', '启用', null, '2025-12-15 19:12:40', '2025-12-15 19:12:40');
INSERT INTO `accounting_subject` VALUES ('1997310346336071695', '1602', '累计折旧', '1', '0', '资产', '启用', null, '2025-12-15 19:12:40', '2025-12-15 19:12:40');
INSERT INTO `accounting_subject` VALUES ('1997310346336071696', '2001', '短期借款', '1', '0', '负债', '启用', null, '2025-12-15 19:12:40', '2025-12-15 19:12:40');
INSERT INTO `accounting_subject` VALUES ('1997310346336071697', '2211', '应付职工薪酬', '1', '0', '负债', '启用', null, '2025-12-15 19:12:40', '2025-12-15 19:12:40');
INSERT INTO `accounting_subject` VALUES ('1997310346336071698', '2241', '其他应付款', '1', '0', '负债', '启用', null, '2025-12-15 19:12:40', '2025-12-15 19:12:40');
INSERT INTO `accounting_subject` VALUES ('1997310346336071699', '4001', '实收资本', '1', '0', '所有者权益', '启用', null, '2025-12-15 19:12:40', '2025-12-15 19:12:40');
INSERT INTO `accounting_subject` VALUES ('1997310346336071700', '4103', '本年利润', '1', '0', '所有者权益', '启用', null, '2025-12-15 19:12:40', '2025-12-15 19:12:40');
INSERT INTO `accounting_subject` VALUES ('1997310346336071701', '4104', '利润分配', '1', '0', '所有者权益', '启用', null, '2025-12-15 19:12:40', '2025-12-15 19:12:40');
INSERT INTO `accounting_subject` VALUES ('1997310346336071702', '5001', '生产成本', '1', '0', '成本', '启用', null, '2025-12-15 19:12:40', '2025-12-15 19:12:40');
INSERT INTO `accounting_subject` VALUES ('1997310346336071703', '5101', '制造费用', '1', '0', '成本', '启用', null, '2025-12-15 19:12:40', '2025-12-15 19:12:40');
INSERT INTO `accounting_subject` VALUES ('1997310346336071704', '6401', '主营业务成本', '1', '0', '损益', '启用', null, '2025-12-15 19:12:40', '2025-12-15 19:12:40');
INSERT INTO `accounting_subject` VALUES ('1997310346336071705', '6402', '其他业务成本', '1', '0', '损益', '启用', null, '2025-12-15 19:12:40', '2025-12-15 19:12:40');
INSERT INTO `accounting_subject` VALUES ('1997310346336071706', '6601', '销售费用', '1', '0', '损益', '启用', null, '2025-12-15 19:12:40', '2025-12-15 19:12:40');
INSERT INTO `accounting_subject` VALUES ('1997310346336071707', '6603', '财务费用', '1', '0', '损益', '启用', null, '2025-12-15 19:12:40', '2025-12-15 19:12:40');
INSERT INTO `accounting_subject` VALUES ('1997310346336071708', '6301', '营业外收入', '1', '0', '损益', '启用', null, '2025-12-15 19:12:40', '2025-12-15 19:12:40');
INSERT INTO `accounting_subject` VALUES ('1997310346336071709', '6711', '营业外支出', '1', '0', '损益', '启用', null, '2025-12-15 19:12:40', '2025-12-15 19:12:40');
INSERT INTO `ap_payable` VALUES ('1', 'AP20251209434', '6', '4', '嘎达沙克', '4.08', '已核销', '1', '2025-12-09 19:10:27', '2025-12-09 19:10:27');
INSERT INTO `ap_payable` VALUES ('2', 'AP20251209575', '7', '2', '6很好听', '200.00', '待确认', '1', '2025-12-09 19:11:05', '2025-12-09 19:11:05');
INSERT INTO `ap_payable` VALUES ('3', 'AP20251209869', '11', '7', 'yyw', '10.10', '待确认', '1', '2025-12-09 19:30:48', '2025-12-09 19:30:48');
INSERT INTO `ap_payable` VALUES ('4', 'AP20251209054', '12', '4', '嘎达沙克', '136.73', '待确认', '1', '2025-12-09 19:35:06', '2025-12-09 19:35:06');
INSERT INTO `ap_payable` VALUES ('5', 'AP20251209085', '13', '7', 'yyw', '1.01', '待确认', '1', '2025-12-09 19:44:09', '2025-12-09 19:44:09');
INSERT INTO `ap_payable` VALUES ('6', 'AP20251209525', '14', '7', 'yyw', '134.31', '已核销', '1', '2025-12-09 19:53:57', '2025-12-09 19:53:57');
INSERT INTO `ap_payable` VALUES ('7', 'AP20251209578', '15', '7', 'yyw', '1.00', '已核销', '1', '2025-12-09 19:56:08', '2025-12-09 19:56:08');
INSERT INTO `ap_payable` VALUES ('8', 'AP20251209316', '16', '7', 'yyw', '29.38', '已核销', '1', '2025-12-09 20:00:10', '2025-12-09 20:00:10');
INSERT INTO `ap_payable` VALUES ('9', 'AP20251209290', '17', '7', 'yyw', '110.00', '已核销', '1', '2025-12-09 20:01:29', '2025-12-09 20:01:29');
INSERT INTO `ap_payable` VALUES ('10', 'AP20251209880', '18', '7', 'yyw', '24.00', '已核销', '1', '2025-12-09 20:07:39', '2025-12-09 20:07:40');
INSERT INTO `ap_payable` VALUES ('11', 'AP20251209469', '19', '7', 'yyw', '22.60', '待确认', '1', '2025-12-09 20:08:32', '2025-12-09 20:08:32');
INSERT INTO `ap_payable` VALUES ('12', 'AP20251210810', '20', '4', '嘎达沙克', '136.73', '待确认', '1', '2025-12-10 19:52:11', '2025-12-10 19:52:11');
INSERT INTO `ap_payable` VALUES ('13', 'AP20251210404', '21', '4', '嘎达沙克', '4.52', '待确认', '1', '2025-12-10 22:47:18', '2025-12-10 22:47:18');
INSERT INTO `ap_payable` VALUES ('14', 'AP20251210625', '22', '2', '6很好听', '12.36', '待确认', '1', '2025-12-10 22:48:19', '2025-12-10 22:48:19');
INSERT INTO `ap_payable` VALUES ('15', 'AP20251218239', '23', '10', '深圳电子元器件贸易公司', '339.00', '已核销', '1', '2025-12-18 20:06:12', '2025-12-18 20:06:12');
INSERT INTO `ap_payable` VALUES ('16', 'AP20251219727', '24', '12', '杭州办公用品有限公司', '226.00', '待确认', '1', '2025-12-19 16:59:12', '2025-12-19 16:59:13');
INSERT INTO `ap_payable` VALUES ('17', 'AP20251219568', '25', '9', '上海机械设备有限公司', '3390.00', '待确认', '1', '2025-12-19 22:00:45', '2025-12-19 22:00:45');
INSERT INTO `ap_payment` VALUES ('1', 'PAY202512090001', '10', '7', 'yyw', '24.00', '2025-12-09 00:00:00', '银行转账', '已确认', '1', '2025-12-09 20:47:12', '2025-12-09 20:47:12');
INSERT INTO `ap_payment` VALUES ('2', 'PAY202512090002', '9', '7', 'yyw', '110.00', '2025-12-09 00:00:00', '现金', '已确认', '1', '2025-12-09 20:48:08', '2025-12-09 20:48:08');
INSERT INTO `ap_payment` VALUES ('3', 'PAY202512100001', '8', '7', 'yyw', '29.38', '2025-12-10 00:00:00', '现金', '已确认', '1', '2025-12-10 10:53:15', '2025-12-10 10:53:15');
INSERT INTO `ap_payment` VALUES ('4', 'PAY202512100002', '7', '7', 'yyw', '1.00', '2025-12-10 00:00:00', '银行转账', '已确认', '1', '2025-12-10 10:55:34', '2025-12-10 10:55:34');
INSERT INTO `ap_payment` VALUES ('5', 'PAY202512100003', '7', '7', 'yyw', '1.00', '2025-12-10 00:00:00', '银行转账', '已确认', '1', '2025-12-10 10:57:18', '2025-12-10 10:57:18');
INSERT INTO `ap_payment` VALUES ('6', 'PAY202512100004', '6', '7', 'yyw', '134.31', '2025-12-10 00:00:00', '银行转账', '已确认', '1', '2025-12-10 19:54:22', '2025-12-10 19:54:22');
INSERT INTO `ap_payment` VALUES ('7', 'PAY202512180001', '1', '4', '嘎达沙克', '4.08', '2025-12-19 00:00:00', '银行转账', '已确认', '1', '2025-12-18 19:54:03', '2025-12-18 19:54:03');
INSERT INTO `ap_payment` VALUES ('8', 'PAY202512180002', '15', '10', '深圳电子元器件贸易公司', '339.00', '2025-12-18 00:00:00', '银行转账', '已确认', '1', '2025-12-18 20:07:45', '2025-12-18 20:07:45');
INSERT INTO `ar_payment` VALUES ('1', 'REC202512150001', '5', '2', '刘佳灵', '10.60', '2025-12-15 00:00:00', '银行转账', '已确认', '1', '2025-12-15 18:06:43', '2025-12-15 18:06:43');
INSERT INTO `ar_payment` VALUES ('2', 'REC202512150002', '6', '2', '刘佳灵', '4.16', '2025-12-15 00:00:00', '现金', '已确认', '1', '2025-12-15 18:08:02', '2025-12-15 18:08:02');
INSERT INTO `ar_payment` VALUES ('3', 'REC202512180001', '4', '1', 'wmz', '113.00', '2025-12-18 00:00:00', '银行转账', '已确认', '1', '2025-12-18 19:40:28', '2025-12-18 19:40:28');
INSERT INTO `ar_payment` VALUES ('4', 'REC202512180002', '3', '1', 'wmz', '1.01', '2025-12-20 00:00:00', '银行转账', '已确认', '1', '2025-12-18 19:40:45', '2025-12-18 19:40:45');
INSERT INTO `ar_payment` VALUES ('5', 'REC202512180003', '2', '1', 'wmz', '1.01', '2025-12-18 00:00:00', '银行转账', '已确认', '1', '2025-12-18 19:40:51', '2025-12-18 19:40:51');
INSERT INTO `ar_payment` VALUES ('6', 'REC202512180004', '2', '1', 'wmz', '1.01', '2025-12-19 00:00:00', '银行转账', '已确认', '1', '2025-12-18 19:40:56', '2025-12-18 19:40:56');
INSERT INTO `ar_payment` VALUES ('7', 'REC202512180005', '1', '2', '刘佳灵', '133.10', '2025-12-18 00:00:00', '银行转账', '已确认', '1', '2025-12-18 19:41:12', '2025-12-18 19:41:12');
INSERT INTO `ar_payment` VALUES ('8', 'REC202512180006', '7', '4', '北京科技有限公司', '15255.00', '2025-12-18 00:00:00', '银行转账', '已确认', '1', '2025-12-18 20:20:13', '2025-12-18 20:20:14');
INSERT INTO `ar_payment` VALUES ('9', 'REC202512200001', '9', '4', '北京科技有限公司', '11300.00', '2025-12-20 00:00:00', '银行转账', '已确认', '1', '2025-12-20 10:33:21', '2025-12-20 10:33:21');
INSERT INTO `ar_payment` VALUES ('10', 'REC202512230001', '10', '4', '北京科技有限公司', '22600.00', '2025-12-23 00:00:00', '银行转账', '已确认', '1', '2025-12-23 12:26:24', '2025-12-23 12:26:24');
INSERT INTO `ar_receivable` VALUES ('1', 'AR20251210001', '3', '2', '刘佳灵', '133.10', '已核销', '1', '2025-12-10 13:01:29', '2025-12-10 13:01:29');
INSERT INTO `ar_receivable` VALUES ('2', 'AR2025121010002', '2', '1', 'wmz', '1.01', '已核销', '1', '2025-12-10 17:47:17', '2025-12-10 17:47:17');
INSERT INTO `ar_receivable` VALUES ('3', 'AR202512101010003', '1', '1', 'wmz', '1.01', '已核销', '1', '2025-12-10 17:49:31', '2025-12-10 17:49:31');
INSERT INTO `ar_receivable` VALUES ('4', 'AR20251210101010004', '4', '1', 'wmz', '113.00', '已核销', '1', '2025-12-10 17:56:11', '2025-12-10 17:56:11');
INSERT INTO `ar_receivable` VALUES ('5', 'AR20251215001', '18', '2', '刘佳灵', '10.60', '已核销', '1', '2025-12-15 10:33:35', '2025-12-15 10:33:35');
INSERT INTO `ar_receivable` VALUES ('6', 'AR2025121515002', '19', '2', '刘佳灵', '4.16', '已核销', '1', '2025-12-15 18:07:52', '2025-12-15 18:07:52');
INSERT INTO `ar_receivable` VALUES ('7', 'AR20251218001', '20', '4', '北京科技有限公司', '15255.00', '已核销', '1', '2025-12-18 20:16:46', '2025-12-18 20:16:46');
INSERT INTO `ar_receivable` VALUES ('8', 'AR2025121818002', '21', '6', '上海贸易有限公司', '5085.00', '待确认', '1', '2025-12-18 20:35:34', '2025-12-18 20:35:34');
INSERT INTO `ar_receivable` VALUES ('9', 'AR20251219001', '22', '4', '北京科技有限公司', '11300.00', '已核销', '1', '2025-12-19 16:54:13', '2025-12-19 16:54:13');
INSERT INTO `ar_receivable` VALUES ('10', 'AR20251223001', '24', '4', '北京科技有限公司', '22600.00', '已核销', '1', '2025-12-23 12:24:58', '2025-12-23 12:24:58');
INSERT INTO `biz_account_mapping` VALUES ('1', 'PURCHASE', '材料采购', '1403', '2202', '采购材料分录', '2025-12-08 17:00:00', '2025-12-08 17:00:00');
INSERT INTO `biz_account_mapping` VALUES ('2', 'PURCHASE', '服务采购', '6602', '2202', '采购服务分录', '2025-12-08 17:00:00', '2025-12-08 17:00:00');
INSERT INTO `biz_account_mapping` VALUES ('3', 'PURCHASE', '固定资产采购', '1601', '2202', '采购固定资产分录', '2025-12-08 17:00:00', '2025-12-08 17:00:00');
INSERT INTO `biz_account_mapping` VALUES ('4', 'PAYMENT', '供应商付款', '2202', '1002', '供应商付款分录', '2025-12-08 17:00:00', '2025-12-08 17:00:00');
INSERT INTO `biz_account_mapping` VALUES ('5', 'SALES', '产品销售', '1122', '6001', '销售产品分录', '2025-12-08 17:00:00', '2025-12-08 17:00:00');
INSERT INTO `biz_account_mapping` VALUES ('6', 'SALES', '服务销售', '1122', '6051', '销售服务分录', '2025-12-08 17:00:00', '2025-12-08 17:00:00');
INSERT INTO `biz_account_mapping` VALUES ('7', 'SALES', '固定资产销售', '1122', '6111', '固定资产销售默认科目映射（应收账款-资产处置收益）', '2025-12-10 18:09:49', '2025-12-10 19:27:22');
INSERT INTO `biz_account_mapping` VALUES ('8', 'RECEIPT', '客户收款', '1002', '1122', '客户收款分录（应收账款-银行存款）', '2025-12-15 18:00:00', '2025-12-15 17:58:35');
INSERT INTO `employee` VALUES ('1', '管理员', '13800138000', '123456', '在职', '1');
INSERT INTO `employee` VALUES ('2', '张三', '13700137003', '123456', '在职', '2');
INSERT INTO `employee` VALUES ('3', '出纳测试', '13900139003', '123456', '在职', '4');
INSERT INTO `employee` VALUES ('4', '采购测试', '13900139002', '123456', '在职', '3');
INSERT INTO `employee` VALUES ('5', '销售', '13900139004', '123456', '在职', '5');
INSERT INTO `employee` VALUES ('6', '陈思', '13755196586', '123456', '停用', '2');
INSERT INTO `employee` VALUES ('7', '陈五', '13800134567', '123456', '停用', '2');
INSERT INTO `journal_entry` VALUES ('1', '', null, '', '', '', '', '0.00', '0.00', '已过账', '', '0', '1', '2025-12-09 11:06:16', '0000-00-00 00:00:00', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('2', 'MANUAL', '0', '1403', '原材料', '2202', '应付账款', '100.00', '0.00', '已过账', '', '0', '1', '2025-12-09 15:20:29', '2025-12-09 15:20:29', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('3', 'MANUAL', '0', '1403', '原材料', '2202', '应付账款', '12.00', '1.00', '已过账', '', '0', '1', '2025-12-09 15:33:52', '2025-12-09 15:33:52', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('4', 'MANUAL', '0', '1002', '银行存款', '1001', '库存现金', '100.00', '0.00', '已过账', '', '1', '1', '2025-12-09 15:36:43', '2025-12-09 15:36:43', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('5', 'MANUAL', '0', '1001', '库存现金', '1002', '银行存款', '1000.00', '0.00', '已过账', '', '0', '1', '2025-12-09 15:39:56', '2025-12-09 15:39:56', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('6', 'MANUAL', '0', '1001', '库存现金', '1002', '银行存款', '1000.00', '0.00', '已过账', '', '0', '1', '2025-12-09 15:42:22', '2025-12-09 15:42:22', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('7', 'MANUAL', '0', '1001', '库存现金', '1002', '银行存款', '1000.00', '0.00', '已过账', '', '0', '1', '2025-12-09 15:44:12', '2025-12-09 15:44:12', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('8', 'MANUAL', '0', '1001', '库存现金', '1002', '银行存款', '2.00', '0.00', '已过账', '', '0', '1', '2025-12-09 16:10:48', '2025-12-09 16:10:48', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('9', 'MANUAL', '0', '1001', '库存现金', '1403', '原材料', '1000.00', '9.00', '已过账', '', '0', '1', '2025-12-09 18:16:03', '2025-12-09 18:16:03', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('10', 'PURCHASE', '18', '6602', '管理费用', '2202', '应付账款', '24.00', '0.00', '已过账', '', '1', '1', '2025-12-09 20:07:40', '2025-12-09 20:07:40', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('11', 'PURCHASE', '18', '22210101', '应交税费-应交增值税-进项税额', '2202', '应付账款', '0.00', '0.00', '已过账', '', '1', '1', '2025-12-09 20:07:40', '2025-12-09 20:07:40', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('12', 'PURCHASE', '19', '1403', '原材料', '2202', '应付账款', '20.00', '0.00', '已过账', '', '1', '1', '2025-12-09 20:08:32', '2025-12-09 20:08:32', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('13', 'PURCHASE', '19', '22210101', '应交税费-应交增值税-进项税额', '2202', '应付账款', '2.60', '2.60', '已过账', '', '1', '1', '2025-12-09 20:08:32', '2025-12-09 20:08:32', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('14', 'payment', '1', '2202', '应付账款', '1002', '银行存款', '24.00', '0.00', '已过账', '', '1', '1', '2025-12-09 20:47:26', '2025-12-09 20:47:26', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('15', 'payment', '2', '2202', '应付账款', '1002', '银行存款', '110.00', '0.00', '已过账', '', '1', '1', '2025-12-09 20:48:21', '2025-12-09 20:48:21', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('16', 'payment', '3', '2202', '应付账款', '1001', '库存现金', '29.38', '0.00', '已过账', '', '1', '1', '2025-12-10 10:53:24', '2025-12-10 10:53:24', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('17', 'payment', '5', '2202', '应付账款', '1002', '银行存款', '1.00', '0.00', '已过账', '', '1', '1', '2025-12-10 10:57:21', '2025-12-10 10:57:21', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('18', 'PURCHASE', '20', '1403', '原材料', '2202', '应付账款', '121.00', '0.00', '已过账', '', '1', '1', '2025-12-10 19:52:11', '2025-12-10 19:52:11', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('19', 'PURCHASE', '20', '22210101', '应交税费-应交增值税-进项税额', '2202', '应付账款', '15.73', '15.73', '已过账', '', '1', '1', '2025-12-10 19:52:11', '2025-12-10 19:52:11', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('20', 'payment', '4', '2202', '应付账款', '1002', '银行存款', '1.00', '0.00', '已过账', '', '1', '1', '2025-12-10 19:52:49', '2025-12-10 19:52:49', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('21', 'payment', '6', '2202', '应付账款', '1002', '银行存款', '134.31', '0.00', '已过账', '', '1', '1', '2025-12-10 19:54:28', '2025-12-10 19:54:28', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('22', 'PURCHASE', '21', '1403', '原材料', '2202', '应付账款', '4.00', '0.00', '已过账', '', '1', '1', '2025-12-10 22:47:18', '2025-12-10 22:47:18', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('23', 'PURCHASE', '21', '22210101', '应交税费-应交增值税-进项税额', '2202', '应付账款', '0.52', '0.52', '已过账', '', '1', '1', '2025-12-10 22:47:18', '2025-12-10 22:47:18', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('24', 'PURCHASE', '22', '1403', '原材料', '2202', '应付账款', '12.00', '0.00', '已过账', '', '1', '1', '2025-12-10 22:48:19', '2025-12-10 22:48:19', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('25', 'PURCHASE', '22', '22210101', '应交税费-应交增值税-进项税额', '2202', '应付账款', '0.36', '0.36', '已过账', '', '1', '1', '2025-12-10 22:48:19', '2025-12-10 22:48:19', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('26', 'SALES', '18', '1122', '应收账款', '6051', '其他业务收入', '10.00', '0.00', '已过账', '销售订单 SO20251215001 生成应收款单 AR20251215001', '1', '1', '2025-12-15 10:33:35', '2025-12-15 10:33:35', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('27', 'SALES', '18', '1122', '应收账款', '22210102', '应交税费-应交增值税-销项税额', '0.60', '0.60', '已过账', '销售订单 SO20251215001 生成应收款单 AR20251215001 (销项税)', '1', '1', '2025-12-15 10:33:35', '2025-12-15 10:33:35', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('28', 'receipt', '1', '1002', '银行存款', '1122', '应收账款', '10.60', '0.00', '已过账', '', '1', '1', '2025-12-15 18:06:44', '2025-12-15 18:06:44', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('29', 'SALES', '19', '1122', '应收账款', '6051', '其他业务收入', '4.00', '0.00', '已过账', '销售订单 SO20251215002 生成应收款单 AR2025121515002', '1', '1', '2025-12-15 18:07:52', '2025-12-15 18:07:52', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('30', 'SALES', '19', '1122', '应收账款', '22210102', '应交税费-应交增值税-销项税额', '0.16', '0.16', '已过账', '销售订单 SO20251215002 生成应收款单 AR2025121515002 (销项税)', '1', '1', '2025-12-15 18:07:52', '2025-12-15 18:07:52', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('31', 'receipt', '2', '1001', '库存现金', '1122', '应收账款', '4.16', '0.00', '已过账', '', '1', '1', '2025-12-15 18:08:13', '2025-12-15 18:08:13', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('32', 'receipt', '3', '1002', '银行存款', '1122', '应收账款', '113.00', '0.00', '已过账', '', '1', '1', '2025-12-18 19:40:35', '2025-12-18 19:40:35', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('33', 'receipt', '4', '1002', '银行存款', '1122', '应收账款', '1.01', '0.00', '已过账', '', '1', '1', '2025-12-18 19:40:48', '2025-12-18 19:40:48', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('34', 'receipt', '6', '1002', '银行存款', '1122', '应收账款', '1.01', '0.00', '已过账', '', '1', '1', '2025-12-18 19:40:59', '2025-12-18 19:40:59', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('35', 'receipt', '5', '1002', '银行存款', '1122', '应收账款', '1.01', '0.00', '已过账', '', '1', '1', '2025-12-18 19:41:01', '2025-12-18 19:41:01', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('36', 'receipt', '7', '1002', '银行存款', '1122', '应收账款', '133.10', '0.00', '已过账', '', '1', '1', '2025-12-18 19:41:14', '2025-12-18 19:41:14', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('37', 'payment', '7', '2202', '应付账款', '1002', '银行存款', '4.08', '0.00', '已过账', '', '1', '1', '2025-12-18 19:54:06', '2025-12-18 19:54:06', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('38', 'PURCHASE', '23', '1403', '原材料', '2202', '应付账款', '300.00', '0.00', '已过账', '', '1', '1', '2025-12-18 20:06:12', '2025-12-18 20:06:12', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('39', 'PURCHASE', '23', '22210101', '应交税费-应交增值税-进项税额', '2202', '应付账款', '39.00', '39.00', '已过账', '', '1', '1', '2025-12-18 20:06:12', '2025-12-18 20:06:12', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('40', 'payment', '8', '2202', '应付账款', '1002', '银行存款', '339.00', '0.00', '已过账', '', '1', '1', '2025-12-18 20:07:47', '2025-12-18 20:07:47', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('41', 'SALES', '20', '1122', '应收账款', '6001', '主营业务收入', '13500.00', '0.00', '已过账', '销售订单 SO20251218001 生成应收款单 AR20251218001', '1', '1', '2025-12-18 20:16:46', '2025-12-18 20:16:46', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('42', 'SALES', '20', '1122', '应收账款', '22210102', '应交税费-应交增值税-销项税额', '1755.00', '1755.00', '已过账', '销售订单 SO20251218001 生成应收款单 AR20251218001 (销项税)', '1', '1', '2025-12-18 20:16:46', '2025-12-18 20:16:46', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('43', 'receipt', '8', '1002', '银行存款', '1122', '应收账款', '15255.00', '0.00', '已过账', '', '1', '1', '2025-12-18 20:20:22', '2025-12-18 20:20:22', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('44', 'SALES', '21', '1122', '应收账款', '6001', '主营业务收入', '4500.00', '0.00', '已过账', '销售订单 SO20251218002 生成应收款单 AR2025121818002', '1', '1', '2025-12-18 20:35:34', '2025-12-18 20:35:34', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('45', 'SALES', '21', '1122', '应收账款', '22210102', '应交税费-应交增值税-销项税额', '585.00', '585.00', '已过账', '销售订单 SO20251218002 生成应收款单 AR2025121818002 (销项税)', '1', '1', '2025-12-18 20:35:34', '2025-12-18 20:35:34', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('46', 'SALES', '22', '1122', '应收账款', '6051', '其他业务收入', '10000.00', '0.00', '已过账', '销售订单 SO20251219001 生成应收款单 AR20251219001', '1', '1', '2025-12-19 16:54:13', '2025-12-19 16:54:13', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('47', 'SALES', '22', '1122', '应收账款', '22210102', '应交税费-应交增值税-销项税额', '1300.00', '1300.00', '已过账', '销售订单 SO20251219001 生成应收款单 AR20251219001 (销项税)', '1', '1', '2025-12-19 16:54:13', '2025-12-19 16:54:13', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('48', 'PURCHASE', '24', '1601', '固定资产', '2202', '应付账款', '200.00', '0.00', '已过账', '', '1', '1', '2025-12-19 16:59:13', '2025-12-19 16:59:13', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('49', 'PURCHASE', '24', '22210101', '应交税费-应交增值税-进项税额', '2202', '应付账款', '26.00', '26.00', '已过账', '', '1', '1', '2025-12-19 16:59:13', '2025-12-19 16:59:13', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('50', 'PURCHASE', '25', '1601', '固定资产', '2202', '应付账款', '3000.00', '0.00', '已过账', '', '1', '1', '2025-12-19 22:00:45', '2025-12-19 22:00:45', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('51', 'PURCHASE', '25', '22210101', '应交税费-应交增值税-进项税额', '2202', '应付账款', '390.00', '390.00', '已过账', '', '1', '1', '2025-12-19 22:00:45', '2025-12-19 22:00:45', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('52', 'receipt', '9', '1002', '银行存款', '1122', '应收账款', '11300.00', '0.00', '已过账', '', '1', '1', '2025-12-20 10:33:25', '2025-12-20 10:33:25', '2025-12-23 10:58:13');
INSERT INTO `journal_entry` VALUES ('53', 'SALES', '24', '1122', '应收账款', '6001', '主营业务收入', '20000.00', '0.00', '已过账', '销售订单 SO20251223001 生成应收款单 AR20251223001', '1', '1', '2025-12-23 12:25:59', '2025-12-23 12:24:58', '2025-12-23 12:24:58');
INSERT INTO `journal_entry` VALUES ('54', 'SALES', '24', '1122', '应收账款', '22210102', '应交税费-应交增值税-销项税额', '2600.00', '2600.00', '已过账', '销售订单 SO20251223001 生成应收款单 AR20251223001 (销项税)', '1', '1', '2025-12-23 12:25:56', '2025-12-23 12:24:58', '2025-12-23 12:24:58');
INSERT INTO `journal_entry` VALUES ('55', 'receipt', '10', '1002', '银行存款', '1122', '应收账款', '22600.00', '0.00', '已过账', '', '1', '1', '2025-12-23 12:32:04', '2025-12-23 12:26:27', '2025-12-23 12:26:27');
INSERT INTO `project` VALUES ('1', 'PRJ-2025001', '企业设备升级改造项目', '张明', '2025-01-05', '2025-12-31', '10000.00', '进行中', '', '0', '2025-12-19 21:49:46', '2025-12-20 00:24:52');
INSERT INTO `purchase_order` VALUES ('24', 'CG20251219784', '12', '杭州办公用品有限公司', '固定资产采购', '226.00', '已审核', '1', '2025-12-19 16:59:03', '2025-12-19 16:59:03', null, null);
INSERT INTO `purchase_order` VALUES ('20', 'CG20251210697', '2', '嘎达沙克', '材料采购', '136.73', '已审核', '1', '2025-12-10 19:52:06', '2025-12-18 19:52:45', null, null);
INSERT INTO `purchase_order` VALUES ('21', 'CG20251210319', '2', '嘎达沙克', '材料采购', '4.52', '已审核', '1', '2025-12-10 22:47:13', '2025-12-18 19:52:56', null, null);
INSERT INTO `purchase_order` VALUES ('22', 'CG20251210553', '2', '6很好听', '材料采购', '12.36', '已审核', '1', '2025-12-10 22:48:14', '2025-12-10 22:48:14', null, null);
INSERT INTO `purchase_order` VALUES ('23', 'CG20251218137', '10', '深圳电子元器件贸易公司', '材料采购', '339.00', '已审核', '1', '2025-12-18 20:06:05', '2025-12-18 20:06:06', null, null);
INSERT INTO `purchase_order` VALUES ('19', 'CG20251209956', '7', 'yyw', '材料采购', '22.60', '已审核', '1', '2025-12-09 20:08:30', '2025-12-09 20:08:30', null, null);
INSERT INTO `purchase_order` VALUES ('17', 'CG20251209568', '7', 'yyw', '普通采购', '110.00', '已审核', '1', '2025-12-09 20:01:18', '2025-12-09 20:01:18', null, null);
INSERT INTO `purchase_order` VALUES ('18', 'CG20251209622', '7', 'yyw', '普通采购', '24.00', '已审核', '1', '2025-12-09 20:07:37', '2025-12-09 20:07:37', null, null);
INSERT INTO `purchase_order` VALUES ('16', 'CG20251209822', '7', 'yyw', '材料采购', '29.38', '已审核', '1', '2025-12-09 20:00:05', '2025-12-09 20:00:05', null, null);
INSERT INTO `purchase_order` VALUES ('15', 'CG20251209573', '7', 'yyw', '服务采购', '1.00', '已审核', '1', '2025-12-09 19:55:53', '2025-12-09 19:55:53', null, null);
INSERT INTO `purchase_order` VALUES ('25', 'CG20251219845', '9', '上海机械设备有限公司', '固定资产采购', '3390.00', '已审核', '1', '2025-12-19 21:59:09', '2025-12-19 21:59:09', '1', '企业设备升级改造项目');
INSERT INTO `purchase_order_line` VALUES ('1', '1', '001', '大沙发', 'pcs', '个', '2.00', '130.00', '260.00', '13.00', '33.80', '2025-12-09 18:50:40', '2025-12-09 18:50:40');
INSERT INTO `purchase_order_line` VALUES ('2', '2', '1', '1', '1', '1', '1.00', '110.00', '110.00', '0.00', '0.00', '2025-12-09 18:50:45', '2025-12-09 18:50:45');
INSERT INTO `purchase_order_line` VALUES ('3', '3', '1', '1', '1', '1', '3.00', '6.00', '18.00', '0.00', '0.00', '2025-12-09 16:13:00', '2025-12-09 16:13:00');
INSERT INTO `purchase_order_line` VALUES ('4', '4', '1', '1', '1', '1', '3.00', '13.00', '39.00', '10.00', '3.90', '2025-12-09 18:54:03', '2025-12-09 18:54:03');
INSERT INTO `purchase_order_line` VALUES ('5', '5', '1', '1', '1', '1', '8.00', '11111.00', '88888.00', '13.00', '11555.44', '2025-12-09 19:04:57', '2025-12-09 19:04:57');
INSERT INTO `purchase_order_line` VALUES ('6', '6', '22', '22', '2', '2', '2.00', '2.00', '4.00', '2.00', '0.08', '2025-12-09 19:10:23', '2025-12-09 19:10:23');
INSERT INTO `purchase_order_line` VALUES ('7', '7', '2', '2', '2', '2', '100.00', '2.00', '200.00', '0.00', '0.00', '2025-12-09 19:11:02', '2025-12-09 19:11:02');
INSERT INTO `purchase_order_line` VALUES ('8', '11', '1', '1', '1', '1', '1.00', '10.00', '10.00', '1.00', '0.10', '2025-12-09 19:30:45', '2025-12-09 19:30:45');
INSERT INTO `purchase_order_line` VALUES ('9', '12', '1', '1', '1', '1', '11.00', '11.00', '121.00', '13.00', '15.73', '2025-12-09 19:35:01', '2025-12-09 19:35:01');
INSERT INTO `purchase_order_line` VALUES ('10', '13', '1', '1', '1', '1', '1.00', '1.00', '1.00', '1.00', '0.01', '2025-12-09 19:44:07', '2025-12-09 19:44:07');
INSERT INTO `purchase_order_line` VALUES ('11', '14', '1', '沙发', 'pcs', '个', '11.00', '11.00', '121.00', '11.00', '13.31', '2025-12-09 19:54:28', '2025-12-09 19:54:28');
INSERT INTO `purchase_order_line` VALUES ('12', '15', '2', '到底', '的', '到底', '1.00', '1.00', '1.00', '0.00', '0.00', '2025-12-09 19:55:53', '2025-12-09 19:55:53');
INSERT INTO `purchase_order_line` VALUES ('13', '16', '001', '沙发', 'pcs', '个', '2.00', '13.00', '26.00', '13.00', '3.38', '2025-12-09 20:00:05', '2025-12-09 20:00:05');
INSERT INTO `purchase_order_line` VALUES ('14', '17', '1', '沙发', 'pcs', '个', '1.00', '110.00', '110.00', '0.00', '0.00', '2025-12-09 20:01:18', '2025-12-09 20:01:18');
INSERT INTO `purchase_order_line` VALUES ('15', '18', '001', '大沙发', 'pcs', '个', '4.00', '6.00', '24.00', '0.00', '0.00', '2025-12-09 20:07:37', '2025-12-09 20:07:37');
INSERT INTO `purchase_order_line` VALUES ('16', '19', '001', '大沙发', 'pcs', '个', '4.00', '5.00', '20.00', '13.00', '2.60', '2025-12-09 20:08:30', '2025-12-09 20:08:30');
INSERT INTO `purchase_order_line` VALUES ('17', '20', '14', '沙发', 'pcs', '个', '11.00', '11.00', '121.00', '13.00', '15.73', '2025-12-18 19:52:45', '2025-12-18 19:52:45');
INSERT INTO `purchase_order_line` VALUES ('18', '21', '1', '对对对', 'pcs', '发', '1.00', '4.00', '4.00', '13.00', '0.52', '2025-12-18 19:52:56', '2025-12-18 19:52:56');
INSERT INTO `purchase_order_line` VALUES ('19', '22', '13', '发', '发', '发', '4.00', '3.00', '12.00', '3.00', '0.36', '2025-12-10 22:48:14', '2025-12-10 22:48:14');
INSERT INTO `purchase_order_line` VALUES ('20', '23', '23', 'USB-C数据线', '1.5m/Type-C', '条', '20.00', '15.00', '300.00', '13.00', '39.00', '2025-12-18 20:06:06', '2025-12-18 20:06:06');
INSERT INTO `purchase_order_line` VALUES ('21', '24', '761', '笔', '', '支', '100.00', '2.00', '200.00', '13.00', '26.00', '2025-12-19 16:59:03', '2025-12-19 16:59:03');
INSERT INTO `purchase_order_line` VALUES ('22', '25', '901', '生产工作台', '', '个', '3.00', '1000.00', '3000.00', '13.00', '390.00', '2025-12-19 21:59:09', '2025-12-19 21:59:09');
INSERT INTO `role` VALUES ('1', 'ADMIN');
INSERT INTO `role` VALUES ('2', 'ACCOUNTANT');
INSERT INTO `role` VALUES ('3', 'PURCHASER');
INSERT INTO `role` VALUES ('4', 'CASHIER');
INSERT INTO `role` VALUES ('5', 'SALES');
INSERT INTO `sales_order` VALUES ('1', 'SO20251210001', '1', 'wmz', '产品销售', '1.01', 'APPROVED', '0', '2025-12-10 12:46:52', '2025-12-10 12:46:52', null, null);
INSERT INTO `sales_order` VALUES ('2', 'SO20251210002', '1', 'wmz', '产品销售', '1.01', 'APPROVED', '0', '2025-12-10 12:46:59', '2025-12-10 12:46:59', null, null);
INSERT INTO `sales_order` VALUES ('3', 'SO20251210003', '2', '刘佳灵', '产品销售', '133.10', 'APPROVED', '0', '2025-12-10 12:50:20', '2025-12-10 12:50:20', null, null);
INSERT INTO `sales_order` VALUES ('4', 'SO20251210004', '1', 'wmz', '产品销售', '113.00', 'APPROVED', '0', '2025-12-10 17:56:07', '2025-12-10 17:56:07', null, null);
INSERT INTO `sales_order` VALUES ('5', 'SO20251210005', '3', 'zsy', '产品销售', '134.31', 'APPROVED', '0', '2025-12-10 18:18:47', '2025-12-10 18:18:47', null, null);
INSERT INTO `sales_order` VALUES ('6', 'SO20251210006', '2', '刘佳灵', '产品销售', '101.00', 'APPROVED', '0', '2025-12-10 18:19:29', '2025-12-10 18:19:29', null, null);
INSERT INTO `sales_order` VALUES ('7', 'SO20251210007', '1', 'wmz', '服务销售', '113.00', 'APPROVED', '0', '2025-12-10 18:24:47', '2025-12-10 18:24:47', null, null);
INSERT INTO `sales_order` VALUES ('8', 'SO20251210008', '2', '刘佳灵', '产品销售', '226.00', 'APPROVED', '0', '2025-12-10 19:13:37', '2025-12-10 19:13:37', null, null);
INSERT INTO `sales_order` VALUES ('9', 'SO20251210009', '2', '刘佳灵', '服务销售', '224.00', 'APPROVED', '0', '2025-12-10 19:31:16', '2025-12-10 19:31:16', null, null);
INSERT INTO `sales_order` VALUES ('10', 'SO20251210010', '2', '刘佳灵', '服务销售', '37.29', 'APPROVED', '0', '2025-12-10 19:33:31', '2025-12-10 19:33:31', null, null);
INSERT INTO `sales_order` VALUES ('11', 'SO20251210011', '2', '刘佳灵', '产品销售', '113.00', 'APPROVED', '0', '2025-12-10 20:20:30', '2025-12-10 20:20:30', null, null);
INSERT INTO `sales_order` VALUES ('12', 'SO20251210012', '2', '刘佳灵', '服务销售', '9.04', 'APPROVED', '0', '2025-12-10 22:17:08', '2025-12-10 22:17:08', null, null);
INSERT INTO `sales_order` VALUES ('13', 'SO20251210013', '2', '刘佳灵', '产品销售', '4.52', 'APPROVED', '0', '2025-12-10 22:21:36', '2025-12-10 22:21:36', null, null);
INSERT INTO `sales_order` VALUES ('14', 'SO20251210014', '2', '刘佳灵', '产品销售', '12.36', 'APPROVED', '0', '2025-12-10 22:34:00', '2025-12-15 10:29:47', null, null);
INSERT INTO `sales_order` VALUES ('15', 'SO20251210015', '2', '刘佳灵', '产品销售', '4.52', 'APPROVED', '0', '2025-12-10 22:36:41', '2025-12-10 22:36:41', null, null);
INSERT INTO `sales_order` VALUES ('16', 'SO20251210016', '1', 'wmz', '服务销售', '10.50', 'APPROVED', '0', '2025-12-10 22:49:15', '2025-12-10 22:49:15', null, null);
INSERT INTO `sales_order` VALUES ('17', 'SO20251210017', '2', '刘佳灵', '服务销售', '1.01', 'APPROVED', '0', '2025-12-10 23:06:24', '2025-12-10 23:06:24', null, null);
INSERT INTO `sales_order` VALUES ('18', 'SO20251215001', '2', '刘佳灵', '服务销售', '10.60', 'APPROVED', '0', '2025-12-15 10:33:33', '2025-12-15 10:33:33', null, null);
INSERT INTO `sales_order` VALUES ('19', 'SO20251215002', '2', '刘佳灵', '服务销售', '4.16', 'APPROVED', '0', '2025-12-15 18:07:48', '2025-12-15 18:07:48', null, null);
INSERT INTO `sales_order` VALUES ('20', 'SO20251218001', '4', '北京科技有限公司', '产品销售', '15255.00', 'APPROVED', '0', '2025-12-18 20:16:42', '2025-12-18 20:16:42', null, null);
INSERT INTO `sales_order` VALUES ('21', 'SO20251218002', '6', '上海贸易有限公司', '产品销售', '5085.00', 'APPROVED', '0', '2025-12-18 20:35:29', '2025-12-18 20:35:29', null, null);
INSERT INTO `sales_order` VALUES ('22', 'SO20251219001', '4', '北京科技有限公司', '服务销售', '11300.00', 'APPROVED', '0', '2025-12-19 16:54:03', '2025-12-19 16:54:03', null, null);
INSERT INTO `sales_order` VALUES ('23', 'SO20251219002', '6', '上海贸易有限公司', '产品销售', '4520.00', 'DRAFT', '0', '2025-12-19 23:52:15', '2025-12-19 23:52:15', null, null);
INSERT INTO `sales_order` VALUES ('24', 'SO20251223001', '4', '北京科技有限公司', '产品销售', '22600.00', 'APPROVED', '0', '2025-12-23 12:24:53', '2025-12-23 12:24:53', null, null);
INSERT INTO `sales_order_line` VALUES ('1', '3', '1', '水', 'pcs', '个', '11.00', '11.00', '121.00', '10.00', '12.10', '2025-12-10 12:50:20', '2025-12-18 19:36:13');
INSERT INTO `sales_order_line` VALUES ('2', '4', '1', '桌子', 'pcs', '个', '1.00', '100.00', '100.00', '13.00', '13.00', '2025-12-10 17:56:07', '2025-12-18 19:36:18');
INSERT INTO `sales_order_line` VALUES ('3', '5', '1', '桌子', '1', 'pcs', '11.00', '11.00', '121.00', '11.00', '13.31', '2025-12-10 18:18:47', '2025-12-18 19:36:22');
INSERT INTO `sales_order_line` VALUES ('4', '6', '1', '沙发', 'pcs', '个', '1.00', '100.00', '100.00', '1.00', '1.00', '2025-12-10 18:19:29', '2025-12-10 18:19:29');
INSERT INTO `sales_order_line` VALUES ('5', '7', '1', '沙发', 'pcs', '个', '1.00', '100.00', '100.00', '13.00', '13.00', '2025-12-10 18:24:47', '2025-12-10 18:24:47');
INSERT INTO `sales_order_line` VALUES ('6', '8', '22', '沙发', 'pcs', '个', '2.00', '100.00', '200.00', '13.00', '26.00', '2025-12-10 19:13:37', '2025-12-10 19:13:37');
INSERT INTO `sales_order_line` VALUES ('7', '9', '1', '沙发', 'pcs', '个', '2.00', '100.00', '200.00', '12.00', '24.00', '2025-12-10 19:31:16', '2025-12-10 19:31:16');
INSERT INTO `sales_order_line` VALUES ('8', '10', '123', '沙发', 'pcs', '个', '1.00', '33.00', '33.00', '13.00', '4.29', '2025-12-10 19:33:31', '2025-12-10 19:33:31');
INSERT INTO `sales_order_line` VALUES ('9', '11', '12', '沙发', 'pcs', '个', '1.00', '100.00', '100.00', '13.00', '13.00', '2025-12-10 20:20:30', '2025-12-10 20:20:30');
INSERT INTO `sales_order_line` VALUES ('10', '12', '4', '西瓜', 'pcs', '个', '2.00', '4.00', '8.00', '13.00', '1.04', '2025-12-10 22:17:08', '2025-12-10 22:17:08');
INSERT INTO `sales_order_line` VALUES ('11', '13', '1', '沙发', 'pcs', '个', '2.00', '2.00', '4.00', '13.00', '0.52', '2025-12-10 22:21:36', '2025-12-10 22:21:36');
INSERT INTO `sales_order_line` VALUES ('16', '14', '1', '订单', '1', '的', '3.00', '4.00', '12.00', '3.00', '0.36', '2025-12-15 10:29:47', '2025-12-15 10:29:47');
INSERT INTO `sales_order_line` VALUES ('13', '15', '1', '沙发', 'pcs', '个', '1.00', '4.00', '4.00', '13.00', '0.52', '2025-12-10 22:36:41', '2025-12-10 22:36:41');
INSERT INTO `sales_order_line` VALUES ('14', '16', '55', '的', '的', '的', '2.00', '5.00', '10.00', '5.00', '0.50', '2025-12-10 22:49:15', '2025-12-10 22:49:15');
INSERT INTO `sales_order_line` VALUES ('15', '17', '1', '的', '的', '的', '1.00', '1.00', '1.00', '1.00', '0.01', '2025-12-10 23:06:24', '2025-12-10 23:06:24');
INSERT INTO `sales_order_line` VALUES ('17', '18', '1', '沙发', 'pcs', '分', '2.00', '5.00', '10.00', '6.00', '0.60', '2025-12-15 10:33:33', '2025-12-15 10:33:33');
INSERT INTO `sales_order_line` VALUES ('18', '19', '1', '沙发', 'pcs', '个', '1.00', '4.00', '4.00', '4.00', '0.16', '2025-12-15 18:07:48', '2025-12-15 18:07:48');
INSERT INTO `sales_order_line` VALUES ('19', '20', 'MAT-2025005', '液晶显示屏', '24V/3A', '个', '300.00', '45.00', '13500.00', '13.00', '1755.00', '2025-12-18 20:16:42', '2025-12-18 20:16:42');
INSERT INTO `sales_order_line` VALUES ('20', '21', 'MAT-2025001', '电源适配器', '24V/3A', '个', '100.00', '45.00', '4500.00', '13.00', '585.00', '2025-12-18 20:35:29', '2025-12-18 20:35:29');
INSERT INTO `sales_order_line` VALUES ('21', '22', '01', '笔记本', 'B5', '台', '10.00', '1000.00', '10000.00', '13.00', '1300.00', '2025-12-19 16:54:03', '2025-12-19 16:54:03');
INSERT INTO `sales_order_line` VALUES ('22', '23', '99', '电脑', '', '台', '2.00', '2000.00', '4000.00', '13.00', '520.00', '2025-12-19 23:52:15', '2025-12-19 23:52:15');
INSERT INTO `sales_order_line` VALUES ('23', '24', '01', '笔记本', 'b5', '台', '10.00', '2000.00', '20000.00', '13.00', '2600.00', '2025-12-23 12:24:53', '2025-12-23 12:24:53');
INSERT INTO `sys_customer` VALUES ('1', '13577762016', 'wmz', '123123123', 'SFSDF', '2025-12-06 16:58:22', '324', '启用', '2025-12-12 16:58:30', '2025-12-06 20:03:09');
INSERT INTO `sys_customer` VALUES ('2', '', '刘佳灵', '', '', null, '', '启用', '2025-12-06 17:31:04', '2025-12-09 15:45:52');
INSERT INTO `sys_customer` VALUES ('3', '18669113106', 'zsy', '3268719464@qq.com', '', '2025-12-09 00:00:00', '', '启用', '2025-12-09 18:29:05', '2025-12-09 18:29:05');
INSERT INTO `sys_customer` VALUES ('4', '13800138001', '北京科技有限公司', 'beijing@example.com', '北京市朝阳区科技园区1号', '2025-12-01 10:00:00', '重要客户，长期合作', '启用', '2025-12-15 10:00:00', '2025-12-15 10:00:00');
INSERT INTO `sys_customer` VALUES ('5', '13900139002', '张三', 'zhangsan@example.com', '上海市浦东新区世纪大道100号', '2025-12-05 14:30:00', '个人客户', '启用', '2025-12-15 10:00:00', '2025-12-15 10:00:00');
INSERT INTO `sys_customer` VALUES ('6', '13700137003', '上海贸易有限公司', '', '上海市黄浦区南京东路200号', '2025-12-08 09:15:00', '新客户，待跟进', '启用', '2025-12-15 10:00:00', '2025-12-15 10:00:00');
INSERT INTO `sys_customer` VALUES ('7', '13600136004', '已停用客户', 'disabled@example.com', '广州市天河区天河路300号', '2025-11-01 08:00:00', '已停止合作', '停用', '2025-12-15 10:00:00', '2025-12-15 10:00:00');
INSERT INTO `sys_customer` VALUES ('9', '13500135005', '李四', 'lisi@example.com', '深圳市南山区科技园南区400号', '2025-12-10 16:20:00', 'VIP客户', '启用', '2025-12-15 10:00:00', '2025-12-15 10:00:00');
INSERT INTO `sys_customer` VALUES ('10', '13400134006', '深圳制造有限公司', 'shenzhen@example.com', '深圳市宝安区工业区500号', '2025-12-12 11:00:00', '批量采购客户，月结30天', '启用', '2025-12-15 10:00:00', '2025-12-15 10:00:00');
INSERT INTO `sys_supplier` VALUES ('2', '嘎达沙克', 'v', '13577752014', '1321312312', null, 'dsfadad', '工商银行', '6222021234567890123', null, '1', '0', '2025-12-05 22:44:33', '2025-12-18 19:51:49');
INSERT INTO `sys_supplier` VALUES ('3', '额达到', '发', '13577762016', null, null, '13131231313', '', '', null, '1', '0', '2025-12-06 11:06:53', '2025-12-06 20:13:39');
INSERT INTO `sys_supplier` VALUES ('4', '嘎达沙克', '刘', '13577762016', null, null, '13577762016', '工商银行', '1323131231313121', null, '1', '0', '2025-12-06 15:40:34', '2025-12-06 20:13:41');
INSERT INTO `sys_supplier` VALUES ('7', 'yyw', 'yyw', '13577777777', null, null, '1323123', '建设银行', '13213123123', null, '1', '0', '2025-12-06 20:15:50', '2025-12-06 20:15:50');
INSERT INTO `sys_supplier` VALUES ('6', '1313', '0', '13577762016', null, null, '01313123', '2121', '1213', null, '1', '0', '2025-12-06 17:35:53', '2025-12-06 20:13:40');
INSERT INTO `sys_supplier` VALUES ('8', '北京原材料供应有限公司', '王经理', '13800138008', 'beijing@supplier.com', '北京市海淀区中关村大街1号', '91110000123456789X', '中国工商银行', '6222021234567890128', '91110000123456789X', '1', '0', '2025-12-15 10:00:00', '2025-12-15 10:00:00');
INSERT INTO `sys_supplier` VALUES ('9', '上海机械设备有限公司', '李总', '13900139009', 'shanghai@supplier.com', '上海市浦东新区张江高科技园区2号', '91310000987654321Y', '中国建设银行', '6227001234567890129', '91310000987654321Y', '1', '0', '2025-12-15 10:00:00', '2025-12-15 10:00:00');
INSERT INTO `sys_supplier` VALUES ('10', '深圳电子元器件贸易公司', '张主管', '13700137010', null, '深圳市南山区科技园南区3号', '91440300112233445Z', '招商银行', '6225881234567890130', null, '1', '0', '2025-12-15 10:00:00', '2025-12-15 10:00:00');
INSERT INTO `sys_supplier` VALUES ('11', '广州包装材料供应商', '陈经理', '13600136011', 'guangzhou@supplier.com', '广州市天河区天河路4号', '91440100223344556A', '中国农业银行', '6228481234567890131', '91440100223344556A', '2', '0', '2025-12-15 10:00:00', '2025-12-15 10:00:00');
INSERT INTO `sys_supplier` VALUES ('12', '杭州办公用品有限公司', '刘主任', '13500135012', null, null, '91330100334455667B', '中国银行', '6217001234567890132', null, '1', '0', '2025-12-15 10:00:00', '2025-12-15 10:00:00');
