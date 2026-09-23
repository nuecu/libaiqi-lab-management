package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.Building;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingRepository extends JpaRepository<Building, Long> {
    Page<Building> findByBuildingNameContaining(String name, Pageable pageable);
}
