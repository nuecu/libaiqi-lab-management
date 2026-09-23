package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.MaterialReturn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MaterialReturnRepository extends JpaRepository<MaterialReturn, Long> {
    Page<MaterialReturn> findByUserId(Long userId, Pageable pageable);
    Page<MaterialReturn> findByStatus(String status, Pageable pageable);
    Page<MaterialReturn> findByConsumableId(Long consumableId, Pageable pageable);
    List<MaterialReturn> findByUserIdOrderByCreateTimeDesc(Long userId);

    // 趋势分析：按创建时间拉取区间内归还单
    List<MaterialReturn> findByCreateTimeAfter(LocalDateTime time);
}
