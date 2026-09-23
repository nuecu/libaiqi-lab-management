package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    Page<Announcement> findByStatus(Integer status, Pageable pageable);
    Page<Announcement> findByTitleContaining(String title, Pageable pageable);

    // 按分类编码统计公告数量（announcement_type 存的就是分类编码）
    long countByAnnouncementType(String announcementType);
}
