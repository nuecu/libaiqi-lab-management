package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.Consumable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ConsumableRepository extends JpaRepository<Consumable, Long> {
    Optional<Consumable> findByConsumableCode(String consumableCode);
    boolean existsByConsumableCode(String consumableCode);
    Page<Consumable> findByConsumableNameContaining(String name, Pageable pageable);
    Page<Consumable> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Consumable> findByIsHazardous(Boolean isHazardous, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Consumable c WHERE c.id = ?1")
    Optional<Consumable> findByIdForUpdate(Long id);

    @Query("SELECT c FROM Consumable c WHERE c.currentStock <= c.minStock AND c.status = 1")
    List<Consumable> findLowStockItems();

    @Query("SELECT c FROM Consumable c WHERE c.currentStock >= c.maxStock AND c.status = 1")
    List<Consumable> findOverStockItems();
}
