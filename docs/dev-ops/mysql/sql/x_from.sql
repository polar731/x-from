-- ----------------------------
-- x-from 表单工程数据库初始化脚本
-- 库：x_from   字符集：utf8mb4
-- ----------------------------
CREATE DATABASE IF NOT EXISTS `x_from` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `x_from`;

-- ----------------------------
-- 1. 表单定义表
-- ----------------------------
DROP TABLE IF EXISTS `form`;
CREATE TABLE `form` (
  `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `form_no`      varchar(32)  NOT NULL                COMMENT '表单编号（分享短码）',
  `title`        varchar(128) NOT NULL                COMMENT '表单标题',
  `description`  varchar(512)          DEFAULT NULL   COMMENT '表单描述',
  `fields_json`  text         NOT NULL                COMMENT '字段定义(JSON数组)',
  `status`       tinyint      NOT NULL DEFAULT '0'    COMMENT '状态：0草稿 1已发布 2已关闭',
  `submit_count` int          NOT NULL DEFAULT '0'    COMMENT '累计提交数',
  `creator`      varchar(64)           DEFAULT NULL   COMMENT '创建人',
  `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_form_no` (`form_no`),
  KEY `idx_status_create_time` (`status`, `create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '表单定义';

-- ----------------------------
-- 2. 表单填写记录表
-- ----------------------------
DROP TABLE IF EXISTS `form_submission`;
CREATE TABLE `form_submission` (
  `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `form_id`     bigint      NOT NULL                COMMENT '表单ID',
  `form_no`     varchar(32) NOT NULL                COMMENT '表单编号',
  `data_json`   text        NOT NULL                COMMENT '填写内容(JSON对象)',
  `submit_ip`   varchar(64)          DEFAULT NULL   COMMENT '提交IP',
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`id`),
  KEY `idx_form_no` (`form_no`),
  KEY `idx_form_id` (`form_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '表单填写记录';
