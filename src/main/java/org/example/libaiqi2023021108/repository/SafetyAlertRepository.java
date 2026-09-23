package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.SafetyAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SafetyAlertRepository extends JpaRepository<SafetyAlert, Long> {
    Page<SafetyAlert> findByAlertType(String alertType, Pageable pageable);
    Page<SafetyAlert> findByStatus(String status, Pageable pageable);
    Page<SafetyAlert> findByAlertLevel(String alertLevel, Pageable pageable);

    // 违约任务去重：判断某预约是否已经生成过同类型预警
    boolean existsByAlertTypeAndRelatedTypeAndRelatedId(String alertType, String relatedType, Long relatedId);
}
