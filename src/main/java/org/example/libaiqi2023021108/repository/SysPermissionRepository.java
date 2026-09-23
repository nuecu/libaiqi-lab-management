package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SysPermissionRepository extends JpaRepository<SysPermission, Long> {
    List<SysPermission> findByParentId(Long parentId);
    List<SysPermission> findByStatus(Integer status);
    List<SysPermission> findByPermissionType(String permissionType);
}
