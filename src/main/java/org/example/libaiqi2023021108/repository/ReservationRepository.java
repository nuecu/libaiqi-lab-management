package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Page<Reservation> findByUserId(Long userId, Pageable pageable);
    Page<Reservation> findByResourceIdAndResourceType(Long resourceId, String resourceType, Pageable pageable);
    Page<Reservation> findByStatus(String status, Pageable pageable);

    // 趋势分析：按创建时间拉取区间内预约，在服务层按天聚合
    List<Reservation> findByCreateTimeAfter(LocalDateTime time);

    @Query("SELECT r FROM Reservation r WHERE r.resourceId = ?1 AND r.resourceType = ?2 AND r.reservationDate = ?3 AND r.status IN ('PENDING', 'APPROVED', 'IN_USE')")
    List<Reservation> findConflicting(Long resourceId, String resourceType, LocalDate date);

    // 规则引擎：统计某用户对某资源在某天的有效预约次数（用于 maxTimesPerDay 限制）
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.userId = ?1 AND r.resourceId = ?2 AND r.resourceType = ?3 AND r.reservationDate = ?4 AND r.status IN ('PENDING', 'APPROVED', 'IN_USE')")
    long countActiveByUserResourceDate(Long userId, Long resourceId, String resourceType, LocalDate date);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.status = ?1")
    long countByStatus(String status);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.reservationDate >= ?1")
    long countByReservationDateSince(LocalDate since);

    @Query("SELECT r FROM Reservation r WHERE r.reservationDate < ?1 AND r.status = 'APPROVED'")
    List<Reservation> findOverdueApproved(LocalDate date);

    // 定时违约任务：仅扫描「今天」已过结束时间仍未签到的已审批预约，避免误伤历史种子数据
    @Query("SELECT r FROM Reservation r WHERE r.status = 'APPROVED' AND r.reservationDate = ?1 AND r.endTime <= ?2")
    List<Reservation> findNoShowToday(LocalDate today, LocalTime now);

    @Query("SELECT r.reservationDate, COUNT(r) FROM Reservation r WHERE r.reservationDate >= ?1 GROUP BY r.reservationDate ORDER BY r.reservationDate")
    List<Object[]> countDailyReservations(LocalDate since);

    // —— 个人统计（按 userId 隔离）——
    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, String status);

    @Query("SELECT r.reservationDate, COUNT(r) FROM Reservation r WHERE r.userId = ?2 AND r.reservationDate >= ?1 GROUP BY r.reservationDate ORDER BY r.reservationDate")
    List<Object[]> countDailyReservationsByUser(LocalDate since, Long userId);
}
