package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.TemporaryRequisition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemporaryRequisitionRepository extends JpaRepository<TemporaryRequisition, Long> {
    Page<TemporaryRequisition> findByUserId(Long userId, Pageable pageable);
    Page<TemporaryRequisition> findByStatus(String status, Pageable pageable);
}
