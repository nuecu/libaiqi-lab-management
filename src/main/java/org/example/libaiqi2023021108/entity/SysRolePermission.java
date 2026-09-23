package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
/**
 * 模块：系统管理
 * 功能：角色-权限关联实体类，维护角色与权限的多对多关系
 */

@Data
@Entity
@Table(name = "sys_role_permission")
public class SysRolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "permission_id", nullable = false)
    private Long permissionId;
}
