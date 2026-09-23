package org.example.libaiqi2023021108.service;

import org.example.libaiqi2023021108.entity.ExperimentLog;
import org.example.libaiqi2023021108.entity.MaterialRequisition;
import org.example.libaiqi2023021108.entity.MaterialReturn;
import org.example.libaiqi2023021108.entity.Reservation;
import org.example.libaiqi2023021108.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
/**
 * 模块：数据统计
 * 功能：数据统计服务
 */

@Service
public class StatisticsService {

    private final ConsumableRepository consumableRepository;
    private final InstrumentRepository instrumentRepository;
    private final ReservationRepository reservationRepository;
    private final ExperimentLogRepository experimentLogRepository;
    private final SafetyAlertRepository safetyAlertRepository;
    private final SysUserRepository userRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final MaterialRequisitionRepository materialRequisitionRepository;
    private final MaterialReturnRepository materialReturnRepository;

    public StatisticsService(ConsumableRepository consumableRepository, InstrumentRepository instrumentRepository,
                             ReservationRepository reservationRepository, ExperimentLogRepository experimentLogRepository,
                             SafetyAlertRepository safetyAlertRepository, SysUserRepository userRepository,
                             LaboratoryRepository laboratoryRepository, MaterialRequisitionRepository materialRequisitionRepository,
                             MaterialReturnRepository materialReturnRepository) {
        this.consumableRepository = consumableRepository;
        this.instrumentRepository = instrumentRepository;
        this.reservationRepository = reservationRepository;
        this.experimentLogRepository = experimentLogRepository;
        this.safetyAlertRepository = safetyAlertRepository;
        this.userRepository = userRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.materialRequisitionRepository = materialRequisitionRepository;
        this.materialReturnRepository = materialReturnRepository;
    }

    /** 获取仪表盘概览数据（耗材/仪器/预约/实验/预警/用户总数等） */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConsumables", consumableRepository.count());
        stats.put("totalInstruments", instrumentRepository.count());
        stats.put("totalReservations", reservationRepository.count());
        stats.put("pendingReservations", reservationRepository.countByStatus("PENDING"));
        stats.put("approvedReservations", reservationRepository.countByStatus("APPROVED"));
        stats.put("totalExperiments", experimentLogRepository.count());
        stats.put("activeAlerts", safetyAlertRepository.findByStatus("ACTIVE", PageRequest.of(0, 1)).getTotalElements());
        stats.put("totalUsers", userRepository.count());
        stats.put("lowStockItems", consumableRepository.findLowStockItems().size());
        // 供前端概览页正确取数：实验室数量、本月预约数（此前前端误用 totalUsers/totalReservations 顶替）
        stats.put("totalLaboratories", laboratoryRepository.count());
        stats.put("monthReservations", reservationRepository.countByReservationDateSince(LocalDate.now().withDayOfMonth(1)));
        return stats;
    }

    /** 获取耗材统计（总类型数、低库存数、超库存数、危化品数） */
    public Map<String, Object> getConsumableStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTypes", consumableRepository.count());
        stats.put("lowStockCount", consumableRepository.findLowStockItems().size());
        stats.put("overStockCount", consumableRepository.findOverStockItems().size());
        stats.put("hazardousCount", consumableRepository.findByIsHazardous(true, PageRequest.of(0, 1)).getTotalElements());
        return stats;
    }

    /** 获取仪器按状态分类统计（空闲/使用中/故障/保养中/已报废） */
    public Map<String, Object> getInstrumentStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", instrumentRepository.count());
        stats.put("idle", instrumentRepository.findByStatus("IDLE", PageRequest.of(0, 1)).getTotalElements());
        stats.put("inUse", instrumentRepository.findByStatus("IN_USE", PageRequest.of(0, 1)).getTotalElements());
        stats.put("fault", instrumentRepository.findByStatus("FAULT", PageRequest.of(0, 1)).getTotalElements());
        stats.put("maintenance", instrumentRepository.findByStatus("MAINTENANCE", PageRequest.of(0, 1)).getTotalElements());
        stats.put("scrapped", instrumentRepository.findByStatus("SCRAPPED", PageRequest.of(0, 1)).getTotalElements());
        return stats;
    }

    /** 获取预约统计（各状态数量及审批通过率） */
    public Map<String, Object> getReservationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", reservationRepository.count());
        stats.put("pending", reservationRepository.countByStatus("PENDING"));
        stats.put("approved", reservationRepository.countByStatus("APPROVED"));
        stats.put("inUse", reservationRepository.countByStatus("IN_USE"));
        stats.put("rejected", reservationRepository.countByStatus("REJECTED"));
        stats.put("cancelled", reservationRepository.countByStatus("CANCELLED"));
        stats.put("completed", reservationRepository.countByStatus("COMPLETED"));
        long total = reservationRepository.count();
        if (total > 0) {
            stats.put("approvalRate", String.format("%.1f%%", (stats.get("approved") != null ? (long)stats.get("approved") : 0) * 100.0 / total));
        }
        return stats;
    }

    /** 获取近30天预约趋势数据（按日期分组统计） */
    public List<Map<String, Object>> getReservationTrend() {
        LocalDate since = LocalDate.now().minusDays(29);
        List<Object[]> rows = reservationRepository.countDailyReservations(since);
        Map<LocalDate, Long> dateMap = rows.stream()
                .collect(Collectors.toMap(r -> (LocalDate) r[0], r -> (Long) r[1]));
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            LocalDate date = since.plusDays(i);
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("count", dateMap.getOrDefault(date, 0L));
            result.add(item);
        }
        return result;
    }

    /**
     * 趋势分析（真实数据）：按创建日期将最近 N 天（week=7 / month=30 / quarter=90）的数据分桶聚合，
     * 输出预约、耗材、实验、仪器使用率、审批效率等多组曲线，替换前端此前的 Math.random 假数据。
     */
    public Map<String, Object> getTrendAnalysis(String range) {
        int days = "week".equals(range) ? 7 : "quarter".equals(range) ? 90 : 30;
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(days - 1L);
        LocalDateTime since = start.atStartOfDay();

        // 初始化 N 个日期桶
        List<String> dates = new ArrayList<>();
        Map<LocalDate, Integer> idx = new HashMap<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = start.plusDays(i);
            idx.put(d, i);
            dates.add(d.toString());
        }
        long[] newRes = new long[days];
        long[] completed = new long[days];
        long[] cancelled = new long[days];
        long[] issueQty = new long[days];
        long[] returnQty = new long[days];
        long[] experiments = new long[days];
        long[] instrResCount = new long[days];
        double[] approvalHoursSum = new double[days];
        long[] approvalCount = new long[days];
        long[] decidedCount = new long[days];
        long[] approvedCount = new long[days];

        // 预约分桶（按创建当天归集，状态取当前值）
        long totalInstruments = instrumentRepository.count();
        for (Reservation r : reservationRepository.findByCreateTimeAfter(since)) {
            if (r.getCreateTime() == null) continue;
            Integer i = idx.get(r.getCreateTime().toLocalDate());
            if (i == null) continue;
            newRes[i]++;
            String st = r.getStatus();
            if ("COMPLETED".equals(st)) completed[i]++;
            else if ("CANCELLED".equals(st)) cancelled[i]++;
            if ("INSTRUMENT".equals(r.getResourceType())) instrResCount[i]++;
            boolean decided = "APPROVED".equals(st) || "IN_USE".equals(st) || "COMPLETED".equals(st) || "REJECTED".equals(st);
            if (decided) {
                decidedCount[i]++;
                if (!"REJECTED".equals(st)) approvedCount[i]++;
            }
            if (r.getApproveTime() != null && r.getCreateTime() != null) {
                double hrs = Duration.between(r.getCreateTime(), r.getApproveTime()).toMinutes() / 60.0;
                if (hrs >= 0) { approvalHoursSum[i] += hrs; approvalCount[i]++; }
            }
        }

        // 实验记录分桶
        for (ExperimentLog l : experimentLogRepository.findByCreateTimeAfter(since)) {
            if (l.getCreateTime() == null) continue;
            Integer i = idx.get(l.getCreateTime().toLocalDate());
            if (i != null) experiments[i]++;
        }

        // 耗材领用分桶（数量取 totalItems 之和）
        for (MaterialRequisition m : materialRequisitionRepository.findByCreateTimeAfter(since)) {
            if (m.getCreateTime() == null) continue;
            Integer i = idx.get(m.getCreateTime().toLocalDate());
            if (i != null) issueQty[i] += (m.getTotalItems() == null ? 0 : m.getTotalItems());
        }

        // 耗材归还分桶（数量取 returnQuantity 之和）
        for (MaterialReturn m : materialReturnRepository.findByCreateTimeAfter(since)) {
            if (m.getCreateTime() == null) continue;
            Integer i = idx.get(m.getCreateTime().toLocalDate());
            if (i != null) returnQty[i] += (m.getReturnQuantity() == null ? 0 : m.getReturnQuantity());
        }

        // 组装结果数组
        List<Long> newL = new ArrayList<>(), compL = new ArrayList<>(), cancelL = new ArrayList<>();
        List<Long> issueL = new ArrayList<>(), retL = new ArrayList<>(), expL = new ArrayList<>();
        List<Double> instrRateL = new ArrayList<>(), hoursL = new ArrayList<>(), passRateL = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            newL.add(newRes[i]); compL.add(completed[i]); cancelL.add(cancelled[i]);
            issueL.add(issueQty[i]); retL.add(returnQty[i]); expL.add(experiments[i]);
            double rate = totalInstruments > 0 ? Math.round(instrResCount[i] * 1000.0 / totalInstruments) / 10.0 : 0.0;
            instrRateL.add(Math.min(rate, 100.0));
            hoursL.add(approvalCount[i] > 0 ? Math.round(approvalHoursSum[i] / approvalCount[i] * 10.0) / 10.0 : 0.0);
            passRateL.add(decidedCount[i] > 0 ? Math.round(approvedCount[i] * 1000.0 / decidedCount[i]) / 10.0 : 0.0);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dates", dates);
        result.put("newReservations", newL);
        result.put("completed", compL);
        result.put("cancelled", cancelL);
        result.put("issueQty", issueL);
        result.put("returnQty", retL);
        result.put("experiments", expL);
        result.put("instrumentRate", instrRateL);
        result.put("approvalHours", hoursL);
        result.put("passRate", passRateL);
        return result;
    }

    /** 个人统计概览：仅统计当前用户本人的预约/实验/领用数据 */
    public Map<String, Object> getMyOverview(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        long total = reservationRepository.countByUserId(userId);
        stats.put("totalReservations", total);
        stats.put("pending", reservationRepository.countByUserIdAndStatus(userId, "PENDING"));
        stats.put("approved", reservationRepository.countByUserIdAndStatus(userId, "APPROVED"));
        stats.put("inUse", reservationRepository.countByUserIdAndStatus(userId, "IN_USE"));
        stats.put("completed", reservationRepository.countByUserIdAndStatus(userId, "COMPLETED"));
        stats.put("rejected", reservationRepository.countByUserIdAndStatus(userId, "REJECTED"));
        stats.put("cancelled", reservationRepository.countByUserIdAndStatus(userId, "CANCELLED"));
        if (total > 0) {
            long decided = reservationRepository.countByUserIdAndStatus(userId, "APPROVED")
                    + reservationRepository.countByUserIdAndStatus(userId, "IN_USE")
                    + reservationRepository.countByUserIdAndStatus(userId, "COMPLETED");
            stats.put("approvalRate", String.format("%.1f%%", decided * 100.0 / total));
        } else {
            stats.put("approvalRate", "0.0%");
        }
        stats.put("totalExperiments", experimentLogRepository.countByUserId(userId));
        stats.put("monthReservations", reservationRepository.countDailyReservationsByUser(LocalDate.now().withDayOfMonth(1), userId)
                .stream().mapToLong(r -> (Long) r[1]).sum());
        long myReq = materialRequisitionRepository.countByUserId(userId);
        stats.put("totalRequisitions", myReq);
        stats.put("reqPending", materialRequisitionRepository.countByUserIdAndStatus(userId, "PENDING"));
        stats.put("reqApproved", materialRequisitionRepository.countByUserIdAndStatus(userId, "APPROVED"));
        stats.put("reqIssued", materialRequisitionRepository.countByUserIdAndStatus(userId, "ISSUED"));
        return stats;
    }

    /** 个人近30天预约趋势 */
    public List<Map<String, Object>> getMyTrend(Long userId) {
        LocalDate since = LocalDate.now().minusDays(29);
        List<Object[]> rows = reservationRepository.countDailyReservationsByUser(since, userId);
        Map<LocalDate, Long> dateMap = rows.stream()
                .collect(Collectors.toMap(r -> (LocalDate) r[0], r -> (Long) r[1]));
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            LocalDate date = since.plusDays(i);
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("count", dateMap.getOrDefault(date, 0L));
            result.add(item);
        }
        return result;
    }
}
