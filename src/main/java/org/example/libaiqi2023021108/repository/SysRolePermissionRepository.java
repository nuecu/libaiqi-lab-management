package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.SysRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SysRolePermissionRepository extends JpaRepository<SysRolePermission, Long> {
    List<SysRolePermission> findByRoleId(Long roleId);
    void deleteByRoleId(Long roleId);
    void deleteByPermissionId(Long permissionId);
}
