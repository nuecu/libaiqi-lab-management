package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.Laboratory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface LaboratoryRepository extends JpaRepository<Laboratory, Long> {
    Page<Laboratory> findByLabNameContaining(String name, Pageable pageable);
    Page<Laboratory> findByBuildingId(Long buildingId, Pageable pageable);
    List<Laboratory> findByStatus(Integer status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Laboratory l WHERE l.id = ?1")
    Optional<Laboratory> findByIdForUpdate(Long id);
}
