package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.ExperimentRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperimentRecordRepository extends JpaRepository<ExperimentRecord, Long> {
    Page<ExperimentRecord> findByExperimentLogId(Long experimentLogId, Pageable pageable);
    Page<ExperimentRecord> findByUserId(Long userId, Pageable pageable);
}
