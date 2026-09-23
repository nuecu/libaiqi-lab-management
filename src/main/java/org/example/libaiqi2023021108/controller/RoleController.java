package org.example.libaiqi2023021108.controller;

import org.example.libaiqi2023021108.common.Result;
import org.example.libaiqi2023021108.entity.SysRole;
import org.example.libaiqi2023021108.service.RoleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
/**
 * 模块：系统管理
 * 功能：角色管理控制器
 * 说明：提供角色的增删改查及权限分配接口
 * 权限：需要 system:role
 */
@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasAuthority('system:role')")
public class RoleController {
    private final RoleService roleService;
    public RoleController(RoleService roleService) { this.roleService = roleService; }

    /** 查询所有角色列表（不分页） */
    @GetMapping
    public Result<List<SysRole>> listAll() { return Result.success(roleService.listAll()); }

    /** 分页查询角色列表 */
    @GetMapping("/page")
    public Result<Page<SysRole>> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return Result.success(roleService.list(PageRequest.of(page, size, Sort.by("id").descending())));
    }

    /** 根据ID查询角色详情 */
    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) { return Result.success(roleService.getById(id)); }

    /** 创建新角色 */
    @PostMapping
    public Result<SysRole> create(@RequestBody SysRole role) { return Result.success(roleService.create(role)); }

    /** 更新角色信息 */
    @PutMapping("/{id}")
    public Result<SysRole> update(@PathVariable Long id, @RequestBody SysRole role) { return Result.success(roleService.update(id, role)); }

    /** 删除角色并清除关联的权限关系 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { roleService.delete(id); return Result.success(); }

    /** 为角色分配权限（先清除旧权限再分配新权限） */
    @PostMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        roleService.assignPermissions(id, body.get("permissionIds")); return Result.success();
    }

    /** 获取角色已分配的权限ID列表 */
    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getPermissionIds(@PathVariable Long id) { return Result.success(roleService.getPermissionIds(id)); }
}
