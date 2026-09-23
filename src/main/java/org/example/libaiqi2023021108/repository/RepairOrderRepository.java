package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.RepairOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepairOrderRepository extends JpaRepository<RepairOrder, Long> {
    Page<RepairOrder> findByInstrumentId(Long instrumentId, Pageable pageable);
    Page<RepairOrder> findByReporterId(Long reporterId, Pageable pageable);
    Page<RepairOrder> findByAssigneeId(Long assigneeId, Pageable pageable);
    Page<RepairOrder> findByStatus(String status, Pageable pageable);
}
