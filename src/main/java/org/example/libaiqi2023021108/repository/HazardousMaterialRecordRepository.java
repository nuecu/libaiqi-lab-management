package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.HazardousMaterialRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HazardousMaterialRecordRepository extends JpaRepository<HazardousMaterialRecord, Long> {
    Page<HazardousMaterialRecord> findByConsumableId(Long consumableId, Pageable pageable);
    Page<HazardousMaterialRecord> findByUserId(Long userId, Pageable pageable);
}
