package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SysRoleRepository extends JpaRepository<SysRole, Long> {
    Optional<SysRole> findByRoleCode(String roleCode);
    boolean existsByRoleCode(String roleCode);
}
