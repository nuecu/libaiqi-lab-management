package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
/**
 * 模块：系统管理
 * 功能：系统权限实体类，定义菜单/按钮权限的名称、编码、类型、层级关系等
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_permission")
public class SysPermission extends BaseEntity {
    @Column(name = "permission_code", unique = true, nullable = false, length = 100)
    private String permissionCode;

    @Column(name = "permission_name", nullable = false, length = 50)
    private String permissionName;

    @Column(name = "permission_type", length = 20)
    private String permissionType; // MENU, BUTTON, API

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "path", length = 200)
    private String path;

    @Column(name = "component", length = 200)
    private String component;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "visible")
    private Boolean visible = true;

    @Column(name = "status")
    private Integer status = 1;
}
