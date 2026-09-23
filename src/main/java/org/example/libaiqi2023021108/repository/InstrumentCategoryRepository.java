package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.InstrumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InstrumentCategoryRepository extends JpaRepository<InstrumentCategory, Long> {
    List<InstrumentCategory> findByParentId(Long parentId);
}
