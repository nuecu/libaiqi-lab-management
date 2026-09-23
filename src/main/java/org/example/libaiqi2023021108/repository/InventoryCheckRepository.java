package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.InventoryCheck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryCheckRepository extends JpaRepository<InventoryCheck, Long> {
    Page<InventoryCheck> findByConsumableId(Long consumableId, Pageable pageable);
    Page<InventoryCheck> findByStatus(String status, Pageable pageable);
}
