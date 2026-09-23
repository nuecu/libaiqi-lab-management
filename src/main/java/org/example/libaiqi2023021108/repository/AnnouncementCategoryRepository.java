package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.AnnouncementCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AnnouncementCategoryRepository extends JpaRepository<AnnouncementCategory, Long> {
    List<AnnouncementCategory> findAllByOrderByIdAsc();
    Optional<AnnouncementCategory> findByCategoryCode(String categoryCode);
    boolean existsByCategoryCode(String categoryCode);
}
