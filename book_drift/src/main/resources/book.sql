/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50527
 Source Host           : localhost:3306
 Source Schema         : book

 Target Server Type    : MySQL
 Target Server Version : 50527
 File Encoding         : 65001

 Date: 14/03/2026 13:57:37
*/

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `book` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `book`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for book_claim_record
-- ----------------------------
DROP TABLE IF EXISTS `book_claim_record`;
CREATE TABLE `book_claim_record`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `book_id` int(11) NOT NULL COMMENT '书籍 ID（关联 book_info.id）',
  `user_id` int(11) NOT NULL COMMENT '认领人 ID（关联 sys_user.id）',
  `claim_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '认领时间（MySQL 5.5.27 使用 timestamp 类型）',
  `return_time` datetime NULL DEFAULT NULL COMMENT '归还时间（NULL=未归还）',
  `due_time` datetime NULL DEFAULT NULL COMMENT '应归还时间（认领时间 +30 天）',
  `is_overdue` tinyint(4) NULL DEFAULT 0 COMMENT '是否超期：0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_book_id`(`book_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '书籍认领/归还记录表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for book_info
-- ----------------------------
DROP TABLE IF EXISTS `book_info`;
CREATE TABLE `book_info`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `book_qrcode` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '书籍专属二维码（唯一，扫码核心）',
  `book_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '书名',
  `author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '作者（非必填，简化输入）',
  `donor_id` int(11) NULL DEFAULT NULL COMMENT '捐赠人 ID（关联 sys_user.id，NULL=匿名捐赠）',
  `donate_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '捐赠时间',
  `current_holder_id` int(11) NULL DEFAULT NULL COMMENT '当前持有者 ID（NULL=未认领）',
  `borrow_deadline` datetime NULL DEFAULT NULL COMMENT '借阅到期时间',
  `book_status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：1-待认领 2-已认领 3-已归还',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_book_qrcode`(`book_qrcode`) USING BTREE,
  INDEX `idx_donor_id`(`donor_id`) USING BTREE,
  INDEX `idx_current_holder`(`current_holder_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '书籍信息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for book_note
-- ----------------------------
DROP TABLE IF EXISTS `book_note`;
CREATE TABLE `book_note`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `book_id` int(11) NOT NULL COMMENT '书籍 ID（关联 book_info.id）',
  `user_id` int(11) NOT NULL COMMENT '笔记作者 ID（关联 sys_user.id）',
  `note_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '笔记内容（MySQL 5.5.27 text 类型适配长文本，兼容）',
  `mark_paragraph` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标注重点段落（非必填）',
  `like_count` int(11) NULL DEFAULT 0 COMMENT '点赞数',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_book_id`(`book_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '读书笔记表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 ID（自增，初学者易操作）',
  `student_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学号（唯一，扫码认领身份验证）',
  `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学生姓名',
  `college` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学院（区分本校学生）',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录密码（简单加密即可）',
  `role` tinyint(4) NOT NULL DEFAULT 2 COMMENT '角色：1-管理员 2-普通用户',
  `borrow_count` int(11) NULL DEFAULT 0 COMMENT '累计借书次数',
  `current_medal_id` int(11) NULL DEFAULT NULL COMMENT '当前佩戴的勋章 ID（关联 user_medal.id）',
  `activity_score` int(11) NULL DEFAULT 0 COMMENT '总活跃度分数',
  `daily_activity_score` int(11) NULL DEFAULT 0 COMMENT '日活跃度分数',
  `weekly_activity_score` int(11) NULL DEFAULT 0 COMMENT '周活跃度分数',
  `monthly_activity_score` int(11) NULL DEFAULT 0 COMMENT '月活跃度分数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_id`(`student_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统用户表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for user_medal
-- ----------------------------
DROP TABLE IF EXISTS `user_medal`;
CREATE TABLE `user_medal`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id` int(11) NOT NULL COMMENT '用户 ID（关联 sys_user.id）',
  `medal_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '勋章名称（如：捐赠之星、借阅达人）',
  `medal_type` tinyint(4) NOT NULL COMMENT '勋章类型：1-捐赠 2-借阅 3-分享笔记',
  `required_count` int(11) NULL DEFAULT NULL COMMENT '所需数量（解锁条件）',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '勋章说明',
  `unlock_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间（MySQL 5.5.27 使用 timestamp 类型）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_medal`(`user_id`, `medal_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户勋章表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for book_note_like
-- ----------------------------
DROP TABLE IF EXISTS `book_note_like`;
CREATE TABLE `book_note_like`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `note_id` int(11) NOT NULL COMMENT '笔记 ID（关联 book_note.id）',
  `user_id` int(11) NOT NULL COMMENT '用户 ID（关联 sys_user.id）',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_note_user`(`note_id`, `user_id`) USING BTREE,
  INDEX `idx_note_id`(`note_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '读书笔记点赞记录表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for feedback
-- ----------------------------
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id` int(11) NOT NULL COMMENT '用户 ID（关联 sys_user.id）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '反馈内容',
  `reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '管理员回复',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待处理，PROCESSED-处理中，CLOSED-已关闭',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `reply_time` datetime NULL DEFAULT NULL COMMENT '回复时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户反馈表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for user_activity_record
-- ----------------------------
DROP TABLE IF EXISTS `user_activity_record`;
CREATE TABLE `user_activity_record`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id` int(11) NOT NULL COMMENT '用户 ID（关联 sys_user.id）',
  `activity_type` tinyint(4) NOT NULL COMMENT '活动类型：1-发布书籍 2-成功借出 3-成功归还/认领 4-发布笔记 5-被点赞',
  `score` int(11) NOT NULL COMMENT '增加的活跃度分数',
  `business_id` int(11) NULL DEFAULT NULL COMMENT '相关业务 ID（书籍 ID、笔记 ID 等）',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户活跃度记录表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for book_statistics
-- ----------------------------
DROP TABLE IF EXISTS `book_statistics`;
CREATE TABLE `book_statistics`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `stat_date` datetime NOT NULL COMMENT '统计时间（精确到秒）',
  `total_books` int(11) NOT NULL DEFAULT 0 COMMENT '总书籍数',
  `available_books` int(11) NOT NULL DEFAULT 0 COMMENT '可借阅书籍数',
  `borrowed_books` int(11) NOT NULL DEFAULT 0 COMMENT '已借阅书籍数',
  `returned_books` int(11) NOT NULL DEFAULT 0 COMMENT '已归还书籍数',
  `total_users` int(11) NOT NULL DEFAULT 0 COMMENT '总用户数',
  `total_borrows` int(11) NOT NULL DEFAULT 0 COMMENT '总借阅次数',
  `total_donations` int(11) NOT NULL DEFAULT 0 COMMENT '总捐赠次数',
  `active_users` int(11) NOT NULL DEFAULT 0 COMMENT '活跃用户数',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_stat_date`(`stat_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '书籍统计数据表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for book_tag
-- ----------------------------
DROP TABLE IF EXISTS `book_tag`;
CREATE TABLE `book_tag`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签名称',
  `tag_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签描述',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tag_name`(`tag_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '书籍标签表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for book_tag_relation
-- ----------------------------
DROP TABLE IF EXISTS `book_tag_relation`;
CREATE TABLE `book_tag_relation`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `book_id` int(11) NOT NULL COMMENT '书籍 ID（关联 book_info.id）',
  `tag_id` int(11) NOT NULL COMMENT '标签 ID（关联 book_tag.id）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_book_id`(`book_id`) USING BTREE,
  INDEX `idx_tag_id`(`tag_id`) USING BTREE,
  UNIQUE INDEX `uk_book_tag`(`book_id`, `tag_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '书籍标签关联表' ROW_FORMAT = Compact;

-- ----------------------------
<<<<<<< HEAD
-- Table structure for announcement
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告内容',
  `publisher_id` int(11) NOT NULL COMMENT '发布人 ID（关联 sys_user.id）',
  `publisher_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发布人姓名',
  `is_published` tinyint(4) NOT NULL DEFAULT 1 COMMENT '是否发布：0-草稿 1-已发布',
  `publish_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_publisher_id`(`publisher_id`) USING BTREE,
  INDEX `idx_publish_time`(`publish_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '公告表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for announcement_read
-- ----------------------------
DROP TABLE IF EXISTS `announcement_read`;
CREATE TABLE `announcement_read`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `announcement_id` int(11) NOT NULL COMMENT '公告 ID（关联 announcement.id）',
  `user_id` int(11) NOT NULL COMMENT '用户 ID（关联 sys_user.id）',
  `read_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_announcement_user`(`announcement_id`, `user_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_announcement_id`(`announcement_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '公告阅读记录表' ROW_FORMAT = Compact;
=======
-- Table structure for ai_chat
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat`;
CREATE TABLE `ai_chat`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id` int(11) NOT NULL COMMENT '用户 ID（关联 sys_user.id）',
  `session_id` bigint(20) NOT NULL COMMENT '会话 ID',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色：user-用户，assistant-AI',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '内容',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_session_id`(`session_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'AI 问答记录表' ROW_FORMAT = Compact;
>>>>>>> extreaf_01

SET FOREIGN_KEY_CHECKS = 1;
