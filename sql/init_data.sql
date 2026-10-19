-- ============================================================
-- 高校实验室耗材与仪器预约管理系统 - 完整初始化脚本
-- 可直接在 Navicat 中运行
-- 默认登录密码均为: root
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS lab_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lab_management;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 清空所有表（避免主键冲突）
TRUNCATE TABLE hazardous_material_record;
TRUNCATE TABLE safety_alert;
TRUNCATE TABLE maintenance_record;
TRUNCATE TABLE maintenance_plan;
TRUNCATE TABLE repair_order;
TRUNCATE TABLE inventory_check;
TRUNCATE TABLE material_return;
TRUNCATE TABLE temporary_requisition;
TRUNCATE TABLE material_requisition_item;
TRUNCATE TABLE material_requisition;
TRUNCATE TABLE experiment_record;
TRUNCATE TABLE experiment_log;
TRUNCATE TABLE reservation;
TRUNCATE TABLE reservation_rule;
TRUNCATE TABLE instrument;
TRUNCATE TABLE instrument_category;
TRUNCATE TABLE workstation;
TRUNCATE TABLE laboratory;
TRUNCATE TABLE building;
TRUNCATE TABLE consumable;
TRUNCATE TABLE consumable_category;
TRUNCATE TABLE login_log;
TRUNCATE TABLE operation_log;
TRUNCATE TABLE announcement;
TRUNCATE TABLE sys_role_permission;
TRUNCATE TABLE sys_permission;
TRUNCATE TABLE sys_user_role;
TRUNCATE TABLE sys_role;
TRUNCATE TABLE sys_user;

-- ============================================================
-- 模块六：系统权限与通用支撑
-- ============================================================

-- 1. 系统用户 (密码均为 root 的BCrypt加密)
INSERT INTO sys_user (id, username, password, real_name, student_no, staff_no, email, phone, department, major, user_type, status, create_time, update_time) VALUES
(1, 'admin', '$2a$10$crawPlNX7AbdQIRdij8hX.brurBMAXRB3ULI53x2zqE.3CdKjHC9.', '系统管理员', NULL, 'T2024001', 'admin@lab.edu.cn', '13800000001', '信息中心', NULL, 'ADMIN', 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(2, 'teacher1', '$2a$10$crawPlNX7AbdQIRdij8hX.brurBMAXRB3ULI53x2zqE.3CdKjHC9.', '张教授', NULL, 'T2024002', 'zhangprof@lab.edu.cn', '13800000002', '化学学院', '应用化学', 'TEACHER', 1, '2024-09-01 09:00:00', '2024-09-01 09:00:00'),
(3, 'teacher2', '$2a$10$crawPlNX7AbdQIRdij8hX.brurBMAXRB3ULI53x2zqE.3CdKjHC9.', '李教授', NULL, 'T2024003', 'liprof@lab.edu.cn', '13800000003', '物理学院', '材料物理', 'TEACHER', 1, '2024-09-02 08:00:00', '2024-09-02 08:00:00'),
(4, 'student1', '$2a$10$crawPlNX7AbdQIRdij8hX.brurBMAXRB3ULI53x2zqE.3CdKjHC9.', '王同学', '2024001', NULL, 'wangstu@lab.edu.cn', '13800000004', '化学学院', '应用化学', 'STUDENT', 1, '2024-09-05 10:00:00', '2024-09-05 10:00:00'),
(5, 'student2', '$2a$10$crawPlNX7AbdQIRdij8hX.brurBMAXRB3ULI53x2zqE.3CdKjHC9.', '赵同学', '2024002', NULL, 'zhaostu@lab.edu.cn', '13800000005', '物理学院', '材料物理', 'STUDENT', 1, '2024-09-05 10:30:00', '2024-09-05 10:30:00'),
(6, 'student3', '$2a$10$crawPlNX7AbdQIRdij8hX.brurBMAXRB3ULI53x2zqE.3CdKjHC9.', '刘同学', '2024003', NULL, 'liustu@lab.edu.cn', '13800000006', '化学学院', '化学工程', 'STUDENT', 1, '2024-09-06 09:00:00', '2024-09-06 09:00:00'),
(7, 'maintainer1', '$2a$10$crawPlNX7AbdQIRdij8hX.brurBMAXRB3ULI53x2zqE.3CdKjHC9.', '陈技师', NULL, 'M2024001', 'chentech@lab.edu.cn', '13800000007', '设备处', NULL, 'MAINTAINER', 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00');

-- 2. 系统角色
INSERT INTO sys_role (id, role_code, role_name, description, status, sort_order, create_time, update_time) VALUES
(1, 'ADMIN', '系统管理员', '拥有全部权限', 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(2, 'TEACHER', '教师', '教师角色，可预约实验室和审批', 1, 2, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(3, 'STUDENT', '学生', '学生角色，可预约使用', 1, 3, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(4, 'MAINTAINER', '设备维护员', '负责设备维修保养', 1, 4, '2024-09-01 08:00:00', '2024-09-01 08:00:00');

-- 3. 用户-角色关联
INSERT INTO sys_user_role (id, user_id, role_id) VALUES
(1, 1, 1), (2, 2, 2), (3, 3, 2), (4, 4, 3), (5, 5, 3), (6, 6, 3), (7, 7, 4);

-- 4. 系统权限
INSERT INTO sys_permission (id, permission_code, permission_name, permission_type, parent_id, path, component, icon, sort_order, visible, status, create_time, update_time) VALUES
(1, 'system', '系统管理', 'MENU', 0, '/system', NULL, 'Setting', 1, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(2, 'system:user', '用户管理', 'MENU', 1, '/system/user', 'system/UserManage', NULL, 1, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(3, 'system:role', '角色管理', 'MENU', 1, '/system/role', 'system/RoleManage', NULL, 2, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(4, 'system:permission', '权限管理', 'MENU', 1, '/system/permission', 'system/PermissionManage', NULL, 3, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(5, 'system:log', '日志管理', 'MENU', 1, '/system/log', 'system/LogManage', NULL, 4, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(6, 'resource', '基础资源', 'MENU', 0, '/resource', NULL, 'Box', 2, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(8, 'resource:consumable', '耗材管理', 'MENU', 6, '/resource/consumable', 'resource/ConsumableManage', NULL, 2, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(9, 'resource:instrument', '仪器管理', 'MENU', 6, '/resource/instrument', 'resource/InstrumentManage', NULL, 3, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(10, 'resource:laboratory', '实验室管理', 'MENU', 6, '/resource/laboratory', 'resource/LaboratoryManage', NULL, 4, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(11, 'reservation', '预约管理', 'MENU', 0, '/reservation', NULL, 'Calendar', 3, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(12, 'reservation:list', '预约列表', 'MENU', 11, '/reservation/list', 'reservation/ReservationList', NULL, 1, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(13, 'reservation:rules', '预约规则', 'MENU', 11, '/reservation/rules', 'reservation/ReservationRules', NULL, 2, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(14, 'experiment', '实验管理', 'MENU', 0, '/experiment', NULL, 'Document', 4, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(15, 'experiment:log', '实验记录', 'MENU', 14, '/experiment/log', 'experiment/ExperimentLog', NULL, 1, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(16, 'experiment:material', '耗材领用', 'MENU', 14, '/experiment/material', 'experiment/MaterialManage', NULL, 2, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(17, 'experiment:inventory', '库存盘点', 'MENU', 14, '/experiment/inventory', 'experiment/InventoryCheck', NULL, 3, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(18, 'equipment', '设备运维', 'MENU', 0, '/equipment', NULL, 'Tools', 5, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(19, 'equipment:repair', '故障报修', 'MENU', 18, '/equipment/repair', 'equipment/RepairManage', NULL, 1, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(20, 'equipment:maintenance', '保养管理', 'MENU', 18, '/equipment/maintenance', 'equipment/MaintenanceManage', NULL, 2, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(21, 'equipment:alert', '安全预警', 'MENU', 18, '/equipment/alert', 'equipment/AlertManage', NULL, 3, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(22, 'equipment:hazardous', '危化品管控', 'MENU', 18, '/equipment/hazardous', 'equipment/HazardousManage', NULL, 4, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(23, 'statistics', '数据统计', 'MENU', 0, '/statistics', NULL, 'DataAnalysis', 6, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(24, 'statistics:overview', '数据概览', 'MENU', 23, '/statistics/overview', 'statistics/StatisticsOverview', NULL, 1, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(25, 'equipment:return', '耗材归还', 'MENU', 18, '/equipment/return', 'equipment/ReturnManage', NULL, 5, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(26, 'announcement', '公告管理', 'MENU', 0, '/announcement', NULL, 'Bell', 7, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(27, 'announcement:manage', '公告管理', 'MENU', 26, '/announcement/manage', 'AnnouncementManage', NULL, 1, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(28, 'announcement:category', '公告分类', 'MENU', 26, '/announcement/category', 'announcement/AnnouncementCategory', NULL, 2, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(29, 'statistics:trend', '趋势分析', 'MENU', 23, '/statistics/trend', 'statistics/StatisticsTrend', NULL, 2, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(30, 'reservation:approve', '预约审批', 'BUTTON', 11, NULL, NULL, NULL, 3, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(32, 'resource:consumable:manage', '耗材维护', 'BUTTON', 8, NULL, NULL, NULL, 1, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(33, 'resource:instrument:manage', '仪器维护', 'BUTTON', 9, NULL, NULL, NULL, 1, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(34, 'resource:laboratory:manage', '实验室维护', 'BUTTON', 10, NULL, NULL, NULL, 1, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(35, 'experiment:approve', '领用审批发放', 'BUTTON', 16, NULL, NULL, NULL, 1, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(36, 'announcement:publish', '公告发布维护', 'BUTTON', 26, NULL, NULL, NULL, 3, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(37, 'statistics:my', '我的数据统计', 'MENU', 23, '/statistics/my', 'statistics/MyStatistics', NULL, 3, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(38, 'ai', 'AI助手', 'MENU', 0, '/ai', NULL, 'MagicStick', 8, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(39, 'ai:assistant', '智能问答', 'MENU', 38, '/ai/assistant', 'views/ai/Assistant.vue', 'ChatDotRound', 1, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(40, 'ai:knowledge', '知识库管理', 'MENU', 38, '/ai/knowledge', 'views/ai/KnowledgeManage.vue', 'Reading', 2, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
(41, 'ai:knowledge:manage', '知识库维护', 'BUTTON', 40, NULL, NULL, NULL, 3, 0, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00');

-- 5. 角色-权限关联（严格对齐论文 表2-1 参与者定义 与 表2-2 用例-参与者）
-- 模型说明：读接口(资源浏览/公告/预约可选项)已放开为 ROLE_AUTHENTICATED，任意登录用户可读；
--          本表的权限码主要用于「菜单可见性」与「写/审批操作」。
-- 依据论文收敛口径：基础资源维护(UC4)、预约审批(UC7)、领用审批(UC10)、库存盘点(UC16)、
--          统计看板(UC18)、公告发布(UC19) 均为「仅系统管理员」；
--          保养管理(UC13)、危化品管控(UC14)、耗材归还(UC15)=管理员+教师+运维；
--          安全预警(UC17)=管理员+运维；知识库管理(UC21)=管理员+教师；智能问答(UC20)=全体。
--          另按讨论口径为全体保留「公告查看」(26,27) 与「我的数据统计」(23,37) 只读入口。
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
-- ADMIN(1): 全部权限(1-41，已移除数据字典 7/31)，含系统管理与所有审批/发布
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,8),(1,9),(1,10),(1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(1,17),(1,18),(1,19),(1,20),(1,21),(1,22),(1,23),(1,24),(1,25),(1,26),(1,27),(1,28),(1,29),(1,30),(1,32),(1,33),(1,34),(1,35),(1,36),(1,37),(1,38),(1,39),(1,40),(1,41),
-- TEACHER(2): 预约申请(UC5)/规则(UC6)、实验签到(UC8)/领用申请(UC9)、报修(UC11)/保养(UC13)/危化品(UC14)/归还(UC15)、智能问答(UC20)/知识库管理(UC21)、公告查看+个人统计。无资源维护/审批/盘点/预警/全局统计/发布。
(2,11),(2,12),(2,13),(2,14),(2,15),(2,16),(2,18),(2,19),(2,20),(2,22),(2,25),(2,23),(2,26),(2,27),(2,37),(2,38),(2,39),(2,40),(2,41),
-- STUDENT(3): 预约申请(UC5)、实验签到(UC8)/领用申请(UC9)、智能问答(UC20)、公告查看+个人统计。无规则/审批/盘点/运维/知识库。
(3,11),(3,12),(3,14),(3,15),(3,16),(3,26),(3,27),(3,23),(3,37),(3,38),(3,39),
-- MAINTAINER(4): 设备运维全部(UC11/12/13/14/15/17：报修/保养/危化品/归还/预警)、智能问答(UC20)、公告查看+个人统计。无基础资源维护、无全局统计看板。
(4,18),(4,19),(4,20),(4,21),(4,22),(4,25),(4,26),(4,27),(4,23),(4,37),(4,38),(4,39);

-- 6. 公告
INSERT INTO announcement (id, title, content, announcement_type, status, publish_time, is_top, create_time, update_time, create_by) VALUES
(1, '实验室开放通知', '本学期实验室自2024年9月15日起全面开放，请各班级按照预约系统预约使用。开放时间：周一至周五 8:00-21:00，周六 9:00-17:00。', 'NOTICE', 1, '2024-09-10 08:00:00', 1, '2024-09-10 08:00:00', '2024-09-10 08:00:00', 'admin'),
(2, '关于规范使用危化品的通知', '为保障实验室安全，即日起所有危化品的领用须经指导教师审批后方可领取，并严格做好使用记录。', 'POLICY', 1, '2024-09-12 10:00:00', 0, '2024-09-12 10:00:00', '2024-09-12 10:00:00', 'admin'),
(3, '仪器维护公告', '分析天平（编号 YQ-001）将于本周三进行年度校准，预计停机1天，请提前安排好实验计划。', 'NOTICE', 1, '2024-09-15 09:00:00', 0, '2024-09-15 09:00:00', '2024-09-15 09:00:00', 'admin');

-- 7. 操作日志
INSERT INTO operation_log (id, user_id, username, operation, method, params, ip, status, cost_time, create_time) VALUES
(1, 1, 'admin', '登录系统', 'POST /api/auth/login', '{}', '192.168.1.100', 1, 120, '2024-09-15 08:30:00'),
(2, 1, 'admin', '新增用户', 'POST /api/users', '{"username":"student3"}', '192.168.1.100', 1, 85, '2024-09-15 09:00:00'),
(3, 2, 'teacher1', '登录系统', 'POST /api/auth/login', '{}', '192.168.1.101', 1, 95, '2024-09-15 09:15:00'),
(4, 4, 'student1', '登录系统', 'POST /api/auth/login', '{}', '192.168.1.102', 1, 110, '2024-09-15 10:00:00'),
(5, 1, 'admin', '修改角色', 'PUT /api/roles/2', '{"roleName":"教师"}', '192.168.1.100', 1, 65, '2024-09-15 10:30:00');

-- 8. 登录日志
INSERT INTO login_log (id, user_id, username, ip, location, browser, os, status, msg, login_time) VALUES
(1, 1, 'admin', '192.168.1.100', '校内办公楼', 'Chrome 128', 'Windows 11', 1, '登录成功', '2024-09-15 08:30:00'),
(2, 2, 'teacher1', '192.168.1.101', '化学学院楼', 'Firefox 130', 'macOS 15', 1, '登录成功', '2024-09-15 09:15:00'),
(3, 4, 'student1', '192.168.1.102', '学生宿舍', 'Chrome 128', 'Windows 11', 1, '登录成功', '2024-09-15 10:00:00'),
(4, 5, 'student2', '192.168.1.103', '学生宿舍', 'Safari 18', 'macOS 15', 1, '登录成功', '2024-09-15 10:20:00'),
(5, 3, 'teacher2', '192.168.1.104', '物理学院楼', 'Edge 128', 'Windows 11', 1, '登录成功', '2024-09-15 11:00:00');

-- ============================================================
-- 模块一：基础资源管理
-- ============================================================

-- 9. 耗材分类
INSERT INTO consumable_category (id, category_name, parent_id, category_level, is_hazardous, sort_order, status, create_time, update_time, create_by) VALUES
(1, '化学试剂', NULL, 1, 0, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(2, '玻璃器皿', NULL, 1, 0, 2, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(3, '实验耗材', NULL, 1, 0, 3, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(4, '危化品', NULL, 1, 1, 4, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(5, '无机试剂', 1, 2, 0, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(6, '有机试剂', 1, 2, 0, 2, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(7, '酸碱类', 4, 2, 1, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(8, '有机溶剂', 4, 2, 1, 2, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin');

-- 12. 耗材数据
INSERT INTO consumable (id, consumable_code, consumable_name, category_id, specification, unit, brand, model, supplier, purchase_price, current_stock, min_stock, max_stock, is_hazardous, storage_location, shelf_life_days, status, create_time, update_time, create_by) VALUES
(1, 'HC-001', '无水乙醇', 8, 'AR 500mL', '瓶', '国药集团', 'AR级', '国药化学试剂有限公司', 25.00, 120, 20, 500, 1, '危化品库A-01', 1095, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(2, 'HC-002', '盐酸', 7, 'AR 500mL 36%', '瓶', '国药集团', 'AR级', '国药化学试剂有限公司', 18.00, 80, 15, 300, 1, '危化品库A-02', 1825, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(3, 'HC-003', '氢氧化钠', 5, 'AR 500g', '瓶', '西陇科学', 'AR级', '西陇化工股份有限公司', 15.00, 95, 20, 400, 0, '化学试剂库B-01', 1825, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(4, 'HC-004', '烧杯 100mL', 2, '100mL 低型', '个', '蜀牛', 'GG-17', '蜀牛玻璃仪器厂', 5.50, 200, 50, 1000, 0, '玻璃器皿库C-01', NULL, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(5, 'HC-005', '量筒 250mL', 2, '250mL A级', '个', '蜀牛', 'GG-17', '蜀牛玻璃仪器厂', 12.00, 85, 20, 500, 0, '玻璃器皿库C-02', NULL, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(6, 'HC-006', '滤纸 中速', 3, 'Φ125mm 100张/盒', '盒', 'Whatman', 'No.2', '沃特曼滤纸代理', 45.00, 60, 10, 200, 0, '实验耗材库D-01', NULL, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(7, 'HC-007', '硫酸铜', 5, 'AR 500g', '瓶', '国药集团', 'AR级', '国药化学试剂有限公司', 22.00, 45, 10, 200, 0, '化学试剂库B-02', 1825, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(8, 'HC-008', '丙酮', 8, 'AR 500mL', '瓶', '国药集团', 'AR级', '国药化学试剂有限公司', 28.00, 35, 10, 200, 1, '危化品库A-03', 1095, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(9, 'HC-009', '一次性手套', 3, 'M号 100只/盒', '盒', 'Ansell', 'Touch-N-Tuff', '安全防护用品公司', 35.00, 150, 30, 500, 0, '实验耗材库D-02', NULL, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(10, 'HC-010', 'NaCl 氯化钠', 5, 'AR 500g', '瓶', '国药集团', 'AR级', '国药化学试剂有限公司', 8.00, 180, 30, 600, 0, '化学试剂库B-03', 3650, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin');

-- 13. 仪器分类
INSERT INTO instrument_category (id, category_name, parent_id, sort_order, status, create_time, update_time, create_by) VALUES
(1, '分析仪器', NULL, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(2, '光学仪器', NULL, 2, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(3, '加热设备', NULL, 3, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(4, '称量设备', NULL, 4, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(5, '分离设备', NULL, 5, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin');

-- 14. 楼栋
INSERT INTO building (id, building_name, building_code, floors, address, status, create_time, update_time, create_by) VALUES
(1, '理学楼', 'LX-001', 6, '校园东区主干道', 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(2, '工学楼', 'GX-001', 5, '校园西区实验区', 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(3, '综合实验楼', 'ZH-001', 8, '校园南区科技园', 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin');

-- 15. 实验室
INSERT INTO laboratory (id, lab_name, lab_code, building_id, floor_number, room_number, capacity, area_sqm, lab_type, manager_id, open_start_time, open_end_time, status, create_time, update_time, create_by) VALUES
(1, '无机化学实验室', 'LAB-A101', 1, 1, 'A101', 30, 120.00, 'TEACHING', 2, '08:00', '21:00', 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(2, '有机化学实验室', 'LAB-A201', 1, 2, 'A201', 25, 100.00, 'TEACHING', 2, '08:00', '21:00', 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(3, '材料分析实验室', 'LAB-B101', 2, 1, 'B101', 20, 90.00, 'RESEARCH', 3, '08:30', '20:00', 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(4, '精密仪器室', 'LAB-B201', 2, 2, 'B201', 15, 80.00, 'RESEARCH', 3, '09:00', '18:00', 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(5, '综合检测实验室', 'LAB-C301', 3, 3, 'C301', 35, 150.00, 'TESTING', 2, '08:00', '21:00', 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin');

-- 16. 工位
INSERT INTO workstation (id, workstation_code, workstation_name, laboratory_id, position_desc, capacity, status, create_time, update_time, create_by) VALUES
(1, 'WS-A101-01', '无机化学实验台A', 1, '靠窗第一排', 6, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(2, 'WS-A101-02', '无机化学实验台B', 1, '靠窗第二排', 6, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(3, 'WS-A201-01', '有机化学实验台A', 2, '中央实验台', 5, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(4, 'WS-B101-01', '材料分析工位A', 3, '仪器旁工位', 2, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(5, 'WS-B201-01', '精密仪器操作台', 4, '防震台区域', 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin');

-- 17. 仪器数据
INSERT INTO instrument (id, instrument_code, instrument_name, category_id, brand, model, specification, purchase_price, purchase_date, supplier, laboratory_id, workstation_id, status, warranty_period_months, create_time, update_time, create_by) VALUES
(1, 'YQ-001', '分析天平', 4, '梅特勒', 'ME204E', '最大称量220g/精度0.1mg', 15000.00, '2023-03-15', '梅特勒-托利多中国', 1, NULL, 'IDLE', 36, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(2, 'YQ-002', '紫外可见分光光度计', 2, '岛津', 'UV-2600', '波长范围190-1100nm', 85000.00, '2022-09-20', '岛津中国有限公司', 3, NULL, 'IDLE', 24, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(3, 'YQ-003', '旋转蒸发仪', 5, '上海亚荣', 'RE-52AA', '5L 防爆型', 12000.00, '2023-06-10', '上海亚荣生化仪器厂', 2, NULL, 'IDLE', 12, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(4, 'YQ-004', '电热鼓风干燥箱', 3, '上海一恒', 'DHG-9140A', '140L 最高温度300°C', 6500.00, '2023-01-08', '上海一恒科学仪器', 1, NULL, 'IDLE', 24, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(5, 'YQ-005', '高效液相色谱仪', 1, '安捷伦', '1260 Infinity II', '四元泵/VWD检测器', 450000.00, '2021-11-01', '安捷伦科技中国', 4, 5, 'IDLE', 36, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(6, 'YQ-006', 'pH计', 1, '梅特勒', 'FE28-Standard', '测量范围0-14pH', 4500.00, '2024-01-15', '梅特勒-托利多中国', 1, NULL, 'IDLE', 12, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(7, 'YQ-007', '马弗炉', 3, '天津泰斯特', 'SX2-4-10', '最高温度1000°C 4L', 8000.00, '2022-05-20', '天津泰斯特仪器', 3, NULL, 'FAULT', 24, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(8, 'YQ-008', '离心机', 5, '湘仪', 'H1650-W', '最高转速16500rpm', 28000.00, '2023-08-12', '湘仪离心机仪器', 5, NULL, 'IDLE', 24, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin');

-- ============================================================
-- 模块二：预约审批与排程
-- ============================================================

-- 18. 预约规则
INSERT INTO reservation_rule (id, rule_name, resource_type, resource_id, available_start_time, available_end_time, advance_days, max_duration_hours, max_times_per_day, min_qualification, auto_approve, status, create_time, update_time, create_by) VALUES
(1, '教学实验室通用规则', 'LABORATORY', NULL, '08:00', '21:00', 7, 4, 2, 'STUDENT', 0, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(2, '科研实验室通用规则', 'LABORATORY', NULL, '08:30', '20:00', 14, 8, 1, 'TEACHER', 0, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(3, '精密仪器预约规则', 'INSTRUMENT', 5, '09:00', '18:00', 7, 3, 1, 'TEACHER', 0, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(4, '通用仪器预约规则', 'INSTRUMENT', NULL, '08:00', '21:00', 3, 4, 2, 'STUDENT', 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin');

-- 19. 预约记录
INSERT INTO reservation (id, reservation_no, user_id, resource_type, resource_id, resource_name, reservation_date, start_time, end_time, experiment_topic, participant_count, participants, status, reject_reason, approver_id, approve_time, check_in_time, check_out_time, create_time, update_time, create_by) VALUES
(1, 'RV-20240916-001', 4, 'LABORATORY', 1, '无机化学实验室', '2024-09-16', '09:00', '12:00', '无机合成实验-硫酸铜结晶', 3, '王同学,赵同学,刘同学', 'COMPLETED', NULL, 2, '2024-09-15 14:00:00', '2024-09-16 08:55:00', '2024-09-16 12:05:00', '2024-09-15 10:00:00', '2024-09-16 12:05:00', 'student1'),
(2, 'RV-20240916-002', 5, 'INSTRUMENT', 2, '紫外可见分光光度计', '2024-09-16', '14:00', '17:00', '样品吸光度测定', 1, NULL, 'COMPLETED', NULL, 3, '2024-09-15 15:00:00', '2024-09-16 13:50:00', '2024-09-16 16:50:00', '2024-09-15 11:00:00', '2024-09-16 16:50:00', 'student2'),
(3, 'RV-20240917-001', 4, 'LABORATORY', 2, '有机化学实验室', '2024-09-17', '09:00', '12:00', '有机物提取实验', 2, '王同学,刘同学', 'APPROVED', NULL, 2, '2024-09-16 09:00:00', NULL, NULL, '2024-09-16 08:00:00', '2024-09-16 09:00:00', 'student1'),
(4, 'RV-20240917-002', 6, 'INSTRUMENT', 5, '高效液相色谱仪', '2024-09-17', '10:00', '13:00', '药物成分分析', 1, NULL, 'PENDING', NULL, NULL, NULL, NULL, NULL, '2024-09-16 14:00:00', '2024-09-16 14:00:00', 'student3'),
(5, 'RV-20240918-001', 5, 'LABORATORY', 3, '材料分析实验室', '2024-09-18', '09:00', '17:00', '材料表面形貌分析', 2, '赵同学,刘同学', 'APPROVED', NULL, 3, '2024-09-17 10:00:00', NULL, NULL, '2024-09-17 08:00:00', '2024-09-17 10:00:00', 'student2'),
(6, 'RV-20240915-001', 4, 'INSTRUMENT', 1, '分析天平', '2024-09-15', '09:00', '10:00', '试剂称量', 1, NULL, 'COMPLETED', NULL, NULL, NULL, '2024-09-15 08:50:00', '2024-09-15 10:00:00', '2024-09-15 08:30:00', '2024-09-15 10:00:00', 'student1'),
(7, 'RV-20240918-002', 6, 'LABORATORY', 5, '综合检测实验室', '2024-09-18', '14:00', '18:00', '水质检测综合实验', 4, '刘同学,王同学,赵同学,张同学', 'REJECTED', '该时段已有其他班级预约', 2, '2024-09-17 11:00:00', NULL, NULL, '2024-09-17 09:00:00', '2024-09-17 11:00:00', 'student3'),
(8, 'RV-20240919-001', 4, 'INSTRUMENT', 8, '离心机', '2024-09-19', '09:00', '11:00', '溶液离心分离', 1, NULL, 'PENDING', NULL, NULL, NULL, NULL, NULL, '2024-09-17 15:00:00', '2024-09-17 15:00:00', 'student1');

-- ============================================================
-- 模块三：实验执行与耗材管控
-- ============================================================

-- 20. 实验记录（签到签退）
INSERT INTO experiment_log (id, reservation_id, user_id, laboratory_id, experiment_topic, start_time, end_time, actual_duration_minutes, status, qr_code, create_time, update_time, create_by) VALUES
(1, 1, 4, 1, '无机合成实验-硫酸铜结晶', '2024-09-16 09:00:00', '2024-09-16 12:00:00', 185, 'COMPLETED', 'QR-EXP-001', '2024-09-16 08:55:00', '2024-09-16 12:05:00', 'student1'),
(2, 2, 5, 3, '样品吸光度测定', '2024-09-16 14:00:00', '2024-09-16 17:00:00', 180, 'COMPLETED', 'QR-EXP-002', '2024-09-16 13:50:00', '2024-09-16 16:50:00', 'student2'),
(3, 6, 4, 1, '试剂称量', '2024-09-15 09:00:00', '2024-09-15 10:00:00', 70, 'COMPLETED', 'QR-EXP-003', '2024-09-15 08:50:00', '2024-09-15 10:00:00', 'student1'),
(4, 3, 4, 2, '有机物提取实验', '2024-09-17 09:00:00', '2024-09-17 12:00:00', NULL, 'IN_PROGRESS', 'QR-EXP-004', '2024-09-17 08:55:00', '2024-09-17 08:55:00', 'student1'),
(5, 5, 5, 3, '材料表面形貌分析', '2024-09-18 09:00:00', '2024-09-18 17:00:00', NULL, 'IN_PROGRESS', 'QR-EXP-005', '2024-09-18 08:50:00', '2024-09-18 08:50:00', 'student2');

-- 21. 实验详细记录
INSERT INTO experiment_record (id, experiment_log_id, user_id, instrument_id, record_content, instrument_status, abnormal_desc, create_time, update_time) VALUES
(1, 1, 4, 1, '完成硫酸铜结晶实验，得到蓝色晶体约5.2g，产率78%', 'NORMAL', NULL, '2024-09-16 12:00:00', '2024-09-16 12:00:00'),
(2, 2, 5, 2, '测定5组样品吸光度，数据记录完整，R²=0.998', 'NORMAL', NULL, '2024-09-16 16:45:00', '2024-09-16 16:45:00'),
(3, 3, 4, 1, '称取NaCl 5.00g、CuSO4·5H2O 12.50g', 'NORMAL', NULL, '2024-09-15 09:50:00', '2024-09-15 09:50:00');

-- 22. 耗材领用单
INSERT INTO material_requisition (id, requisition_no, user_id, experiment_log_id, experiment_project, status, approver_id, approve_time, total_items, create_time, update_time, create_by) VALUES
(1, 'MR-20240916-001', 4, 1, '无机合成实验-硫酸铜结晶', 'ISSUED', 2, '2024-09-15 15:00:00', 3, '2024-09-15 10:30:00', '2024-09-16 08:30:00', 'student1'),
(2, 'MR-20240916-002', 5, 2, '样品吸光度测定', 'ISSUED', 3, '2024-09-15 16:00:00', 2, '2024-09-15 11:30:00', '2024-09-16 13:30:00', 'student2'),
(3, 'MR-20240917-001', 4, 4, '有机物提取实验', 'APPROVED', 2, '2024-09-16 17:00:00', 2, '2024-09-16 09:00:00', '2024-09-16 17:00:00', 'student1'),
(4, 'MR-20240918-001', 6, NULL, '水质检测综合实验', 'PENDING', NULL, NULL, 3, '2024-09-17 10:00:00', '2024-09-17 10:00:00', 'student3');

-- 23. 耗材领用明细
INSERT INTO material_requisition_item (id, requisition_id, consumable_id, consumable_name, request_quantity, actual_quantity, unit) VALUES
(1, 1, 7, '硫酸铜', 20, 20, 'g'),
(2, 1, 3, '氢氧化钠', 10, 10, 'g'),
(3, 1, 4, '烧杯 100mL', 2, 2, '个'),
(4, 2, 10, 'NaCl 氯化钠', 50, 50, 'g'),
(5, 2, 6, '滤纸 中速', 1, 1, '盒'),
(6, 3, 1, '无水乙醇', 100, 100, 'mL'),
(7, 3, 8, '丙酮', 50, 0, 'mL'),
(8, 4, 2, '盐酸', 30, NULL, 'mL'),
(9, 4, 3, '氢氧化钠', 20, NULL, 'g'),
(10, 4, 9, '一次性手套', 1, NULL, '盒');

-- 24. 临时领用
INSERT INTO temporary_requisition (id, requisition_no, user_id, consumable_id, consumable_name, quantity, reason, status, approver_id, approve_time, create_time, update_time, create_by) VALUES
(1, 'TR-20240916-001', 4, 4, '烧杯 100mL', 3, '实验过程中额外需要', 'APPROVED', 2, '2024-09-16 10:00:00', '2024-09-16 09:30:00', '2024-09-16 10:00:00', 'student1'),
(2, 'TR-20240917-001', 5, 9, '一次性手套', 2, '实验手套损耗', 'APPROVED', 3, '2024-09-17 11:00:00', '2024-09-17 10:30:00', '2024-09-17 11:00:00', 'student2');

-- 25. 耗材归还
INSERT INTO material_return (id, return_no, user_id, consumable_id, consumable_name, return_quantity, return_type, status, receiver_id, create_time, update_time, create_by) VALUES
(1, 'RT-20240916-001', 4, 7, '硫酸铜', 5, 'REMAINING', 'COMPLETED', 2, '2024-09-16 12:30:00', '2024-09-16 12:30:00', 'student1'),
(2, 'RT-20240916-002', 5, 10, 'NaCl 氯化钠', 15, 'REMAINING', 'COMPLETED', 3, '2024-09-16 17:00:00', '2024-09-16 17:00:00', 'student2');

-- 26. 库存盘点
INSERT INTO inventory_check (id, check_no, check_name, check_date, checker_id, consumable_id, system_quantity, actual_quantity, difference, loss_reason, status, create_time, update_time, create_by) VALUES
(1, 'IC-20240920-001', '9月化学试剂盘点', '2024-09-20', 2, 1, 120, 118, -2, '实验损耗误差', 'COMPLETED', '2024-09-20 09:00:00', '2024-09-20 11:00:00', 'admin'),
(2, 'IC-20240920-002', '9月化学试剂盘点', '2024-09-20', 2, 3, 95, 95, 0, NULL, 'COMPLETED', '2024-09-20 09:00:00', '2024-09-20 11:00:00', 'admin'),
(3, 'IC-20240920-003', '9月玻璃器皿盘点', '2024-09-20', 2, 4, 200, 197, -3, '实验中破损3个', 'COMPLETED', '2024-09-20 14:00:00', '2024-09-20 16:00:00', 'admin'),
(4, 'IC-20240920-004', '9月危化品盘点', '2024-09-21', 1, 2, 80, 79, -1, '挥发损耗', 'COMPLETED', '2024-09-21 09:00:00', '2024-09-21 10:30:00', 'admin');

-- ============================================================
-- 模块四：设备运维与安全管理
-- ============================================================

-- 27. 故障报修工单
INSERT INTO repair_order (id, order_no, instrument_id, instrument_name, reporter_id, fault_desc, fault_image, assignee_id, repair_desc, repair_result, repair_cost, status, complete_time, create_time, update_time, create_by) VALUES
(1, 'RP-20240910-001', 7, '马弗炉', 3, '马弗炉加热丝断裂，无法升温至设定温度，显示屏报E03错误代码', NULL, 7, '更换加热丝和温控模块，重新校准温度传感器', '已修复，温度控制精度恢复到±2°C', 1200.00, 'COMPLETED', '2024-09-12 16:00:00', '2024-09-10 09:00:00', '2024-09-12 16:00:00', 'teacher2'),
(2, 'RP-20240918-001', 3, '旋转蒸发仪', 2, '真空泵密封不良，出现漏气现象，真空度无法维持', NULL, 7, NULL, NULL, NULL, 'PENDING', NULL, '2024-09-18 10:00:00', '2024-09-18 10:00:00', 'teacher1'),
(3, 'RP-20240915-001', 6, 'pH计', 4, 'pH计读数不稳定，波动范围超过0.5', NULL, 7, '更换电极，重新校准', '恢复正常使用', 350.00, 'COMPLETED', '2024-09-16 11:00:00', '2024-09-15 14:00:00', '2024-09-16 11:00:00', 'student1');

-- 28. 保养计划
INSERT INTO maintenance_plan (id, plan_name, instrument_id, maintenance_type, cycle_days, last_maintenance_date, next_maintenance_date, responsible_id, status, create_time, update_time, create_by) VALUES
(1, '分析天平季度保养', 1, 'QUARTERLY', 90, '2024-06-15', '2024-09-15', 7, 'ACTIVE', '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(2, '液相色谱仪月度保养', 5, 'MONTHLY', 30, '2024-08-20', '2024-09-20', 7, 'ACTIVE', '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(3, '分光光度计半年保养', 2, 'YEARLY', 180, '2024-03-10', '2024-09-10', 7, 'ACTIVE', '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(4, '干燥箱季度保养', 4, 'QUARTERLY', 90, '2024-07-01', '2024-10-01', 7, 'ACTIVE', '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin'),
(5, '离心机月度保养', 8, 'MONTHLY', 30, '2024-08-25', '2024-09-25', 7, 'ACTIVE', '2024-09-01 08:00:00', '2024-09-01 08:00:00', 'admin');

-- 29. 保养记录
INSERT INTO maintenance_record (id, plan_id, instrument_id, maintenance_date, maintainer_id, maintenance_content, result, next_maintenance_date, create_time, update_time, create_by) VALUES
(1, 1, 1, '2024-06-15', 7, '清洁称量盘、校准零点、检查水平泡、测试线性', '各项指标正常，通过校准', '2024-09-15', '2024-06-15 10:00:00', '2024-06-15 10:00:00', 'maintainer1'),
(2, 2, 5, '2024-08-20', 7, '更换流动相滤芯、清洗管路、检查泵密封、运行系统适用性测试', '系统适用性测试通过，基线平稳', '2024-09-20', '2024-08-20 14:00:00', '2024-08-20 14:00:00', 'maintainer1'),
(3, 3, 2, '2024-03-10', 7, '更换氘灯、校准波长、清洁光学元件、检查杂散光', '氘灯能量正常，波长精度±0.5nm', '2024-09-10', '2024-03-10 09:00:00', '2024-03-10 09:00:00', 'maintainer1'),
(4, 1, 1, '2024-09-15', 7, '季度保养：清洁、校准、检查防风罩、测试重复性', ' calibration通过，重复性RSD<0.05%', '2024-12-15', '2024-09-15 10:00:00', '2024-09-15 10:00:00', 'maintainer1');

-- 30. 安全预警
INSERT INTO safety_alert (id, alert_type, alert_level, alert_content, related_id, related_type, status, resolve_time, resolver_id, create_time, update_time, create_by) VALUES
(1, 'INVENTORY_LOW', 'MEDIUM', '耗材「丙酮」(HC-008)当前库存35瓶，低于安全库存阈值(10瓶)，请及时补充', 8, 'CONSUMABLE', 'ACTIVE', NULL, NULL, '2024-09-18 08:00:00', '2024-09-18 08:00:00', 'system'),
(2, 'INSTRUMENT_OVERTIME', 'HIGH', '马弗炉(YQ-007)持续故障超过7天未修复，请尽快安排维修', 7, 'INSTRUMENT', 'RESOLVED', '2024-09-12 16:00:00', 7, '2024-09-17 08:00:00', '2024-09-12 16:00:00', 'system'),
(3, 'HAZARDOUS_OVERUSE', 'HIGH', '危化品「无水乙醇」本周领用量达到100mL，超过单人周限额，需额外审批', 1, 'CONSUMABLE', 'ACKNOWLEDGED', NULL, NULL, '2024-09-16 15:00:00', '2024-09-16 15:00:00', 'system'),
(4, 'NO_SHOW', 'LOW', '预约RV-20240918-002（综合检测实验室）未按时签到，已自动取消', 7, 'RESERVATION', 'ACTIVE', NULL, NULL, '2024-09-18 14:30:00', '2024-09-18 14:30:00', 'system'),
(5, 'INVENTORY_LOW', 'LOW', '耗材「滤纸 中速」(HC-006)当前库存60盒，接近安全库存阈值(10盒)', 6, 'CONSUMABLE', 'ACTIVE', NULL, NULL, '2024-09-19 08:00:00', '2024-09-19 08:00:00', 'system');

-- 31. 危化品管控记录
INSERT INTO hazardous_material_record (id, consumable_id, consumable_name, user_id, operation_type, quantity, dual_confirm, confirm_user1_id, confirm_user2_id, qualification_verified, usage_tracking, create_time, update_time, create_by) VALUES
(1, 1, '无水乙醇', 4, 'ISSUE', 100, 1, 2, 4, 1, '用于有机物提取实验，使用80mL，剩余20mL已归还', '2024-09-17 09:30:00', '2024-09-17 09:30:00', 'student1'),
(2, 8, '丙酮', 5, 'ISSUE', 50, 1, 3, 5, 1, '用于样品清洗，使用45mL，废液已收集处理', '2024-09-16 14:30:00', '2024-09-16 14:30:00', 'student2'),
(3, 2, '盐酸', 4, 'ISSUE', 30, 1, 2, 4, 1, '用于酸碱滴定实验，使用25mL', '2024-09-18 09:30:00', '2024-09-18 09:30:00', 'student1'),
(4, 1, '无水乙醇', 4, 'RETURN', 20, 1, 2, 4, 1, '实验剩余归还', '2024-09-17 12:00:00', '2024-09-17 12:00:00', 'student1'),
(5, 8, '丙酮', 5, 'DISPOSE', 5, 1, 3, 7, 1, '废液处理，交由有资质单位处置', '2024-09-16 17:00:00', '2024-09-16 17:00:00', 'student2');

-- ============================================================
-- 模块五：数据统计与报表（统计模块从已有数据动态查询，无需额外数据表）
-- 以下查询语句可用于验证数据统计功能
-- ============================================================

-- 统计查询：各实验室预约次数
-- SELECT l.lab_name, COUNT(r.id) AS reservation_count
-- FROM reservation r JOIN laboratory l ON r.resource_id = l.id
-- WHERE r.resource_type = 'LABORATORY'
-- GROUP BY l.lab_name;

-- 统计查询：各仪器使用时长
-- SELECT i.instrument_name, SUM(TIMESTAMPDIFF(MINUTE, r.start_time, r.end_time)) AS total_minutes
-- FROM reservation r JOIN instrument i ON r.resource_id = i.id
-- WHERE r.resource_type = 'INSTRUMENT' AND r.status = 'COMPLETED'
-- GROUP BY i.instrument_name;

-- 统计查询：耗材领用排行
-- SELECT c.consumable_name, SUM(mri.actual_quantity) AS total_used
-- FROM material_requisition_item mri JOIN consumable c ON mri.consumable_id = c.id
-- GROUP BY c.consumable_name ORDER BY total_used DESC;

-- 统计查询：月度预约趋势
-- SELECT DATE_FORMAT(reservation_date, '%Y-%m') AS month, COUNT(*) AS count
-- FROM reservation
-- GROUP BY month ORDER BY month;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 数据初始化完成！
-- 默认账号：
--   管理员: admin / root
--   教师:   teacher1 / root  teacher2 / root
--   学生:   student1 / root  student2 / root  student3 / root
--   维护员: maintainer1 / root
-- ============================================================
