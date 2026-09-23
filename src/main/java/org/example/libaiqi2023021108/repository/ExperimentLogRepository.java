package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.ExperimentLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExperimentLogRepository extends JpaRepository<ExperimentLog, Long> {
    Page<ExperimentLog> findByUserId(Long userId, Pageable pageable);
    Page<ExperimentLog> findByLaboratoryId(Long laboratoryId, Pageable pageable);
    Page<ExperimentLog> findByStatus(String status, Pageable pageable);

    Optional<ExperimentLog> findByReservationId(Long reservationId);

    long countByUserId(Long userId);

    // 趋势分析：按创建时间拉取区间内实验记录
    List<ExperimentLog> findByCreateTimeAfter(LocalDateTime time);
}
