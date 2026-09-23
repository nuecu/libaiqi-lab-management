package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SysUserRoleRepository extends JpaRepository<SysUserRole, Long> {
    List<SysUserRole> findByUserId(Long userId);
    List<SysUserRole> findByRoleId(Long roleId);
    void deleteByUserId(Long userId);
}
