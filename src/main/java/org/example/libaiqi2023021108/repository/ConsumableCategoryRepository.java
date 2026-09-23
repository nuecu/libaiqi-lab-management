package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.ConsumableCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConsumableCategoryRepository extends JpaRepository<ConsumableCategory, Long> {
    List<ConsumableCategory> findByParentId(Long parentId);
    List<ConsumableCategory> findByIsHazardous(Boolean isHazardous);
}
