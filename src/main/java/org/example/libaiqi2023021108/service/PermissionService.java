package org.example.libaiqi2023021108.service;

import org.example.libaiqi2023021108.common.BusinessException;
import org.example.libaiqi2023021108.entity.SysPermission;
import org.example.libaiqi2023021108.repository.SysPermissionRepository;
import org.example.libaiqi2023021108.repository.SysRolePermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * 模块：系统管理
 * 功能：权限管理服务
 */

@Service
public class PermissionService {

    private final SysPermissionRepository permissionRepository;
    private final SysRolePermissionRepository rolePermissionRepository;
    private final PermissionResolverService permissionResolverService;

    public PermissionService(SysPermissionRepository permissionRepository,
                             SysRolePermissionRepository rolePermissionRepository,
                             PermissionResolverService permissionResolverService) {
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionResolverService = permissionResolverService;
    }

    public List<SysPermission> listAll() {
        return permissionRepository.findAll();
    }

    /** 获取菜单树（返回已启用的顶级菜单节点） */
    public List<SysPermission> getMenuTree() {
        List<SysPermission> all = permissionRepository.findByStatus(1);
        return all.stream().filter(p -> p.getParentId() == null || p.getParentId() == 0).toList();
    }

    /** 获取指定菜单的子菜单列表 */
    public List<SysPermission> getChildren(Long parentId) {
        return permissionRepository.findByParentId(parentId);
    }

    public SysPermission getById(Long id) {
        return permissionRepository.findById(id).orElseThrow(() -> new BusinessException("权限不存在"));
    }

    public SysPermission create(SysPermission permission) {
        return permissionRepository.save(permission);
    }

    public SysPermission update(Long id, SysPermission permission) {
        SysPermission existing = getById(id);
        existing.setPermissionName(permission.getPermissionName());
        existing.setPermissionCode(permission.getPermissionCode());
        existing.setPermissionType(permission.getPermissionType());
        existing.setParentId(permission.getParentId());
        existing.setPath(permission.getPath());
        existing.setComponent(permission.getComponent());
        existing.setIcon(permission.getIcon());
        existing.setSortOrder(permission.getSortOrder());
        existing.setVisible(permission.getVisible());
        existing.setStatus(permission.getStatus());
        SysPermission saved = permissionRepository.save(existing);
        permissionResolverService.evictAllUserPermissions();
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        rolePermissionRepository.deleteByPermissionId(id);
        permissionRepository.deleteById(id);
        permissionResolverService.evictAllUserPermissions();
    }
}
