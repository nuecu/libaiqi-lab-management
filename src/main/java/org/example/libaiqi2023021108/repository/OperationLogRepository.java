package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    Page<OperationLog> findByUsernameContaining(String username, Pageable pageable);
    Page<OperationLog> findByOperationContaining(String operation, Pageable pageable);
}
