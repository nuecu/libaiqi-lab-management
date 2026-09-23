package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.Workstation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkstationRepository extends JpaRepository<Workstation, Long> {
    List<Workstation> findByLaboratoryId(Long laboratoryId);
    Page<Workstation> findByWorkstationNameContaining(String name, Pageable pageable);
}
