package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.MaterialRequisitionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaterialRequisitionItemRepository extends JpaRepository<MaterialRequisitionItem, Long> {
    List<MaterialRequisitionItem> findByRequisitionId(Long requisitionId);
}
