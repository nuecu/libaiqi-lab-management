package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.MaintenancePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface MaintenancePlanRepository extends JpaRepository<MaintenancePlan, Long> {
    List<MaintenancePlan> findByInstrumentId(Long instrumentId);
    List<MaintenancePlan> findByStatus(String status);

    @Query("SELECT m FROM MaintenancePlan m WHERE m.nextMaintenanceDate <= ?1 AND m.status = 'ACTIVE'")
    List<MaintenancePlan> findOverdue(LocalDate date);
}
