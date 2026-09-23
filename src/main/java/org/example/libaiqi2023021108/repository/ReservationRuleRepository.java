package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.ReservationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservationRuleRepository extends JpaRepository<ReservationRule, Long> {
    List<ReservationRule> findByResourceType(String resourceType);
    List<ReservationRule> findByResourceId(Long resourceId);
}
