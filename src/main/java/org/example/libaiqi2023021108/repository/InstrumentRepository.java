package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.Instrument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface InstrumentRepository extends JpaRepository<Instrument, Long> {
    Optional<Instrument> findByInstrumentCode(String instrumentCode);
    boolean existsByInstrumentCode(String instrumentCode);
    Page<Instrument> findByInstrumentNameContaining(String name, Pageable pageable);
    Page<Instrument> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Instrument> findByLaboratoryId(Long laboratoryId, Pageable pageable);
    Page<Instrument> findByStatus(String status, Pageable pageable);
    List<Instrument> findByStatusIn(List<String> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Instrument i WHERE i.id = ?1")
    Optional<Instrument> findByIdForUpdate(Long id);
}
