-- ============================================================
-- 补丁：补齐 @PreAuthorize 引用但数据库缺失的 8 个「写/审批/发布/AI」权限码
-- 背景：运行库由早期 init_data.sql 初始化(仅到 id=29)，而这批操作码从未进入运行库，
--       导致所有用户(含 ADMIN)访问相关写/审批/发布/AI 接口一律 403。
-- 特性：可重复执行(基于 permission_code UNIQUE + role_permission 主键幂等)。
-- 用法：执行本脚本后需 flushall 清 userPermissions 缓存并重启后端。
-- ============================================================
USE lab_management;

INSERT IGNORE INTO sys_permission
    (id, permission_code, permission_name, permission_type, parent_id, path, component, icon, sort_order, visible, status, create_time, update_time)
VALUES
    (38, 'reservation:approve',        '预约审批',     'BUTTON', 11, NULL, NULL, NULL, 3, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
    (39, 'resource:laboratory:manage', '实验室维护',   'BUTTON', 10, NULL, NULL, NULL, 1, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
    (40, 'resource:instrument:manage', '仪器维护',     'BUTTON',  9, NULL, NULL, NULL, 1, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
    (41, 'resource:consumable:manage', '耗材维护',     'BUTTON',  8, NULL, NULL, NULL, 1, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
    (42, 'experiment:approve',         '领用审批发放', 'BUTTON', 16, NULL, NULL, NULL, 1, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
    (43, 'announcement:publish',       '公告发布维护', 'BUTTON', 26, NULL, NULL, NULL, 3, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
    (44, 'ai:assistant',               'AI助手对话',   'BUTTON',  0, '/ai/assistant', 'AIAssistant', 'ChatDotRound', 8, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00'),
    (45, 'ai:knowledge:manage',        'AI知识库维护', 'BUTTON', 44, NULL, 'AIKnowledgeManage', NULL, 1, 1, 1, '2024-09-01 08:00:00', '2024-09-01 08:00:00');

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT r.role_id, p.id FROM (
    SELECT 1 AS role_id, 'reservation:approve' AS code UNION ALL
    SELECT 1, 'resource:laboratory:manage' UNION ALL SELECT 1, 'resource:instrument:manage' UNION ALL
    SELECT 1, 'resource:consumable:manage' UNION ALL SELECT 1, 'experiment:approve' UNION ALL
    SELECT 1, 'announcement:publish' UNION ALL SELECT 1, 'ai:assistant' UNION ALL
    SELECT 1, 'ai:knowledge:manage' UNION ALL
    SELECT 2, 'reservation:approve' UNION ALL SELECT 2, 'resource:laboratory:manage' UNION ALL
    SELECT 2, 'resource:instrument:manage' UNION ALL SELECT 2, 'resource:consumable:manage' UNION ALL
    SELECT 2, 'experiment:approve' UNION ALL SELECT 2, 'announcement:publish' UNION ALL
    SELECT 2, 'ai:assistant' UNION ALL
    SELECT 3, 'ai:assistant' UNION ALL
    SELECT 4, 'resource:consumable:manage'
) r JOIN sys_permission p ON p.permission_code = r.code;

SELECT '八码是否齐备(应为8)' AS item, COUNT(*) AS cnt FROM sys_permission
  WHERE permission_code IN ('reservation:approve','resource:laboratory:manage','resource:instrument:manage','resource:consumable:manage','experiment:approve','announcement:publish','ai:assistant','ai:knowledge:manage');
