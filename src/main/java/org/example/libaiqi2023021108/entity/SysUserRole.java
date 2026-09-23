package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
/**
 * 模块：系统管理
 * 功能：用户-角色关联实体类，维护用户与角色的多对多关系
 */

@Data
@Entity
@Table(name = "sys_user_role")
public class SysUserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;
}
