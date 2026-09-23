package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.SysUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    Optional<SysUser> findByUsername(String username);
    boolean existsByUsername(String username);
    Page<SysUser> findByRealNameContainingOrUsernameContaining(String realName, String username, Pageable pageable);
    Page<SysUser> findByUserType(String userType, Pageable pageable);
    Page<SysUser> findByDepartment(String department, Pageable pageable);
}
