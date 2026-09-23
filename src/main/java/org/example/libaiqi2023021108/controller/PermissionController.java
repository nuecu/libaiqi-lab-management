package org.example.libaiqi2023021108.controller;

import org.example.libaiqi2023021108.common.Result;
import org.example.libaiqi2023021108.entity.SysPermission;
import org.example.libaiqi2023021108.service.PermissionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * 模块：系统管理
 * 功能：权限管理控制器
 * 说明：提供菜单权限的增删改查接口，支持获取菜单树结构
 * 权限：需要 system:permission
 */
@RestController
@RequestMapping("/api/permissions")
@PreAuthorize("hasAuthority('system:permission')")
public class PermissionController {
    private final PermissionService permissionService;
    public PermissionController(PermissionService permissionService) { this.permissionService = permissionService; }

    /** 查询所有权限列表 */
    @GetMapping
    public Result<List<SysPermission>> listAll() { return Result.success(permissionService.listAll()); }

    /** 获取菜单树结构（仅返回已启用的顶级菜单） */
    @GetMapping("/menu-tree")
    public Result<List<SysPermission>> getMenuTree() { return Result.success(permissionService.getMenuTree()); }

    /** 根据ID查询权限详情 */
    @GetMapping("/{id}")
    public Result<SysPermission> getById(@PathVariable Long id) { return Result.success(permissionService.getById(id)); }

    /** 创建新权限（菜单/按钮） */
    @PostMapping
    public Result<SysPermission> create(@RequestBody SysPermission p) { return Result.success(permissionService.create(p)); }

    /** 更新权限信息 */
    @PutMapping("/{id}")
    public Result<SysPermission> update(@PathVariable Long id, @RequestBody SysPermission p) { return Result.success(permissionService.update(id, p)); }

    /** 删除权限 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { permissionService.delete(id); return Result.success(); }
}
