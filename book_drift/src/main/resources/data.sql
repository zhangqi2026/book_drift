/*
 初始数据插入脚本
*/

USE `book`;

-- 插入管理员用户
INSERT INTO `sys_user` (`student_id`, `name`, `college`, `password`, `role`, `borrow_count`) VALUES
('admin', '管理员', '系统管理', 'admin123', 1, 0);

-- 插入普通用户
INSERT INTO `sys_user` (`student_id`, `name`, `college`, `password`, `role`, `borrow_count`) VALUES
('2026001', '张三', '计算机学院', '123456', 2, 0),
('2026002', '李四', '文学院', '123456', 2, 0);

