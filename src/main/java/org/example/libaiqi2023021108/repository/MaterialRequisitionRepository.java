package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.MaterialRequisition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MaterialRequisitionRepository extends JpaRepository<MaterialRequisition, Long> {
    Page<MaterialRequisition> findByUserId(Long userId, Pageable pageable);
    Page<MaterialRequisition> findByStatus(String status, Pageable pageable);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, String status);

    // 趋势分析：按创建时间拉取区间内领用单
    List<MaterialRequisition> findByCreateTimeAfter(LocalDateTime time);
}
