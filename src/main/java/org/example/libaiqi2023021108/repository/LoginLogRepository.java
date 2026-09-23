package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.LoginLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
    Page<LoginLog> findByUsernameContaining(String username, Pageable pageable);
}
