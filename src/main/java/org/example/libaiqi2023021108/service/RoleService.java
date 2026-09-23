package org.example.libaiqi2023021108.service;

import org.example.libaiqi2023021108.common.BusinessException;
import org.example.libaiqi2023021108.entity.SysRole;
import org.example.libaiqi2023021108.entity.SysRolePermission;
import org.example.libaiqi2023021108.repository.SysRoleRepository;
import org.example.libaiqi2023021108.repository.SysRolePermissionRepository;
import org.example.libaiqi2023021108.repository.SysUserRoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * 模块：系统管理
 * 功能：角色管理服务
 */

@Service
public class RoleService {

    private final SysRoleRepository roleRepository;
    private final SysRolePermissionRepository rolePermissionRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final PermissionResolverService permissionResolverService;

    public RoleService(SysRoleRepository roleRepository, SysRolePermissionRepository rolePermissionRepository,
                       SysUserRoleRepository userRoleRepository, PermissionResolverService permissionResolverService) {
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRoleRepository = userRoleRepository;
        this.permissionResolverService = permissionResolverService;
    }

    public Page<SysRole> list(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    public List<SysRole> listAll() {
        return roleRepository.findAll();
    }

    public SysRole getById(Long id) {
        return roleRepository.findById(id).orElseThrow(() -> new BusinessException("角色不存在"));
    }

    /** 创建角色，角色编码不能重复 */
    public SysRole create(SysRole role) {
        if (roleRepository.existsByRoleCode(role.getRoleCode())) {
            throw new BusinessException("角色编码已存在");
        }
        return roleRepository.save(role);
    }

    /** 更新角色信息（名称、描述、状态、排序） */
    public SysRole update(Long id, SysRole role) {
        SysRole existing = getById(id);
        existing.setRoleName(role.getRoleName());
        existing.setDescription(role.getDescription());
        existing.setStatus(role.getStatus());
        existing.setSortOrder(role.getSortOrder());
        return roleRepository.save(existing);
    }

    /** 删除角色并清除关联的权限关系 */
    @Transactional
    public void delete(Long id) {
        if (!userRoleRepository.findByRoleId(id).isEmpty()) {
            throw new BusinessException("该角色已分配给用户，无法删除");
        }
        rolePermissionRepository.deleteByRoleId(id);
        roleRepository.deleteById(id);
        permissionResolverService.evictAllUserPermissions();
    }

    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionRepository.deleteByRoleId(roleId);
        if (permissionIds != null) {
            for (Long permId : permissionIds) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permId);
                rolePermissionRepository.save(rp);
            }
        }
        permissionResolverService.evictAllUserPermissions();
    }

    /** 获取角色已分配的权限ID列表 */
    public List<Long> getPermissionIds(Long roleId) {
        return rolePermissionRepository.findByRoleId(roleId).stream()
                .map(SysRolePermission::getPermissionId).toList();
    }
}
