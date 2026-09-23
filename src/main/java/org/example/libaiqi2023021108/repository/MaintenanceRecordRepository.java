package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.MaintenanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {
    List<MaintenanceRecord> findByInstrumentId(Long instrumentId);
    Page<MaintenanceRecord> findByMaintainerId(Long maintainerId, Pageable pageable);
}
