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

-- 插入默认标签
INSERT INTO `book_tag` (`tag_name`, `tag_desc`) VALUES
('文学小说', '经典文学、小说类书籍'),
('科技编程', '计算机、编程、技术类书籍'),
('历史人文', '历史、人文、社科类书籍'),
('经济管理', '经济、管理、商业类书籍'),
('艺术设计', '艺术、设计、创意类书籍'),
('生活健康', '生活、健康、养生类书籍'),
('教育学习', '教育、学习、考试类书籍'),
('其他', '其他类别的书籍');

