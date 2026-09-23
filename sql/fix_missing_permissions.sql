-- ============================================================
-- 补丁：补齐 sys_permission 缺失的 5 条权限（id 25-29）及角色授权
--
-- 背景：数据库是按早期版本的 init_data.sql 初始化的，只写入到 id=24，
--      而当前 init_data.sql 已扩展到 29 条。缺失的 5 条中
--      announcement:manage 与 statistics:trend 正被控制层
--      @PreAuthorize("hasAuthority(...)") 使用，导致：
--        管理员登录后点击「公告管理」「趋势分析」必返回 403。
--
-- 特性：可重复执行（INSERT IGNORE + 先删后插），不会影响用户/角色等其它数据。
-- 用法：mysql -uroot -proot lab_management < fix_missing_permissions.sql
-- ============================================================

USE lab_management;

-- 1. 补齐缺失的权限记录
INSERT IGNORE INTO sys_permission
    (id, permission_code, permission_name, permission_type, parent_id, path, component, icon, sort_order, visible, status, create_time, update_time)
VALUES
    (25, 'equipment:return',      '耗材归还', 'MENU', 18, '/equipment/return',     'equipment/ReturnManage',             NULL,   5, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
    (26, 'announcement',          '公告管理', 'MENU',  0, '/announcement',         NULL,                                 'Bell', 7, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
    (27, 'announcement:manage',   '公告管理', 'MENU', 26, '/announcement/manage',  'AnnouncementManage',                 NULL,   1, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
    (28, 'announcement:category', '公告分类', 'MENU', 26, '/announcement/category','announcement/AnnouncementCategory',  NULL,   2, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
    (29, 'statistics:trend',      '趋势分析', 'MENU', 23, '/statistics/trend',     'statistics/StatisticsTrend',         NULL,   2, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00');

-- 2. 角色授权（与 init_data.sql 中 sys_role_permission 的定义保持一致）
--    ADMIN(1)      : 全部 5 条
--    TEACHER(2)    : 全部 5 条
--    STUDENT(3)    : 公告 3 条 + 趋势分析（不含 equipment:return）
--    MAINTAINER(4) : 仅 equipment:return
DELETE FROM sys_role_permission WHERE permission_id BETWEEN 25 AND 29;

INSERT INTO sys_role_permission (role_id, permission_id) VALUES
    (1,25),(1,26),(1,27),(1,28),(1,29),
    (2,25),(2,26),(2,27),(2,28),(2,29),
    (3,26),(3,27),(3,28),(3,29),
    (4,25);

-- 3. 旧库残留：STUDENT 缺 experiment:inventory(17)，与 init_data.sql 定义对齐
INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 3, 17 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t WHERE EXISTS (
    SELECT 1 FROM sys_role_permission WHERE role_id = 3 AND permission_id = 17));

-- 4. 校验：ADMIN 应为 29 条，sys_permission 总数应为 29
SELECT 'sys_permission 总数' AS item, COUNT(*) AS cnt FROM sys_permission
UNION ALL
SELECT 'ADMIN 权限数', COUNT(*) FROM sys_role_permission WHERE role_id = 1
UNION ALL
SELECT 'TEACHER 权限数', COUNT(*) FROM sys_role_permission WHERE role_id = 2
UNION ALL
SELECT 'STUDENT 权限数', COUNT(*) FROM sys_role_permission WHERE role_id = 3
UNION ALL
SELECT 'MAINTAINER 权限数', COUNT(*) FROM sys_role_permission WHERE role_id = 4;
