package org.example.libaiqi2023021108.service;

import org.example.libaiqi2023021108.common.BusinessException;
import org.example.libaiqi2023021108.common.CommonUtils;
import org.example.libaiqi2023021108.dto.ReservationDTO;
import org.example.libaiqi2023021108.entity.ExperimentLog;
import org.example.libaiqi2023021108.entity.Instrument;
import org.example.libaiqi2023021108.entity.Laboratory;
import org.example.libaiqi2023021108.entity.Reservation;
import org.example.libaiqi2023021108.entity.ReservationRule;
import org.example.libaiqi2023021108.entity.SysUser;
import org.example.libaiqi2023021108.repository.ExperimentLogRepository;
import org.example.libaiqi2023021108.repository.InstrumentRepository;
import org.example.libaiqi2023021108.repository.LaboratoryRepository;
import org.example.libaiqi2023021108.repository.ReservationRepository;
import org.example.libaiqi2023021108.repository.ReservationRuleRepository;
import org.example.libaiqi2023021108.repository.SysUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
/**
 * 模块：预约管理
 * 功能：预约管理服务
 */

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationRuleRepository ruleRepository;
    private final SysUserRepository userRepository;
    private final ExperimentLogRepository experimentLogRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final InstrumentRepository instrumentRepository;

    public ReservationService(ReservationRepository reservationRepository, ReservationRuleRepository ruleRepository,
                              SysUserRepository userRepository, ExperimentLogRepository experimentLogRepository,
                              LaboratoryRepository laboratoryRepository, InstrumentRepository instrumentRepository) {
        this.reservationRepository = reservationRepository;
        this.ruleRepository = ruleRepository;
        this.userRepository = userRepository;
        this.experimentLogRepository = experimentLogRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.instrumentRepository = instrumentRepository;
    }

    // Rules
    public List<ReservationRule> listRules(String resourceType) {
        if (resourceType != null) return ruleRepository.findByResourceType(resourceType);
        return ruleRepository.findAll();
    }
    public ReservationRule createRule(ReservationRule r) { return ruleRepository.save(r); }
    /** 更新预约规则（名称、时间范围、提前天数、最大时长等） */
    public ReservationRule updateRule(Long id, ReservationRule r) {
        ReservationRule ex = ruleRepository.findById(id).orElseThrow(() -> new BusinessException("规则不存在"));
        ex.setRuleName(r.getRuleName()); ex.setResourceType(r.getResourceType()); ex.setResourceId(r.getResourceId());
        ex.setAvailableStartTime(r.getAvailableStartTime()); ex.setAvailableEndTime(r.getAvailableEndTime());
        ex.setAdvanceDays(r.getAdvanceDays()); ex.setMaxDurationHours(r.getMaxDurationHours());
        ex.setMaxTimesPerDay(r.getMaxTimesPerDay()); ex.setMinQualification(r.getMinQualification());
        ex.setAutoApprove(r.getAutoApprove()); ex.setStatus(r.getStatus());
        return ruleRepository.save(ex);
    }
    public void deleteRule(Long id) { ruleRepository.deleteById(id); }

    // Reservations
    /** 查询预约列表（DTO格式，包含预约人姓名） */
    public Page<ReservationDTO> listWithDetails(Long userId, String status, Pageable pageable) {
        Page<Reservation> page;
        if (userId != null) page = reservationRepository.findByUserId(userId, pageable);
        else if (status != null && !status.isBlank()) page = reservationRepository.findByStatus(status, pageable);
        else page = reservationRepository.findAll(pageable);
        return page.map(this::convertToDTO);
    }

    /** 将预约实体转换为DTO，补充预约人姓名 */
    private ReservationDTO convertToDTO(Reservation r) {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(r.getId());
        dto.setReservationNo(r.getReservationNo());
        dto.setUserId(r.getUserId());
        dto.setResourceType(r.getResourceType());
        dto.setResourceId(r.getResourceId());
        dto.setResourceName(r.getResourceName());
        dto.setReservationDate(r.getReservationDate());
        dto.setStartTime(r.getStartTime());
        dto.setEndTime(r.getEndTime());
        dto.setExperimentTopic(r.getExperimentTopic());
        dto.setParticipantCount(r.getParticipantCount());
        dto.setParticipants(r.getParticipants());
        dto.setStatus(r.getStatus());
        dto.setRejectReason(r.getRejectReason());
        dto.setApproverId(r.getApproverId());
        dto.setApproveTime(r.getApproveTime());
        dto.setCheckInTime(r.getCheckInTime());
        dto.setCheckOutTime(r.getCheckOutTime());
        userRepository.findById(r.getUserId()).ifPresent(user ->
            dto.setUserName(user.getRealName() != null ? user.getRealName() : user.getUsername())
        );
        return dto;
    }

    /** 按资源查询预约列表（查询某仪器/实验室的预约记录） */
    public Page<Reservation> listByResource(Long resourceId, String resourceType, Pageable pageable) {
        return reservationRepository.findByResourceIdAndResourceType(resourceId, resourceType, pageable);
    }

    public Reservation getById(Long id) {
        return reservationRepository.findById(id).orElseThrow(() -> new BusinessException("预约不存在"));
    }

    /**
     * 解析命中的预约规则：优先匹配针对具体资源(resourceId)的规则，
     * 其次回退到该资源类型下 resourceId 为空、status=1 且 id 最小的兜底规则。
     */
    private ReservationRule resolveRule(String resourceType, Long resourceId) {
        if (resourceId != null) {
            Optional<ReservationRule> exact = ruleRepository.findByResourceId(resourceId).stream()
                    .filter(rule -> rule.getStatus() != null && rule.getStatus() == 1)
                    .min(Comparator.comparing(ReservationRule::getId));
            if (exact.isPresent()) return exact.get();
        }
        if (resourceType == null) return null;
        return ruleRepository.findByResourceType(resourceType).stream()
                .filter(rule -> rule.getResourceId() == null && rule.getStatus() != null && rule.getStatus() == 1)
                .min(Comparator.comparing(ReservationRule::getId))
                .orElse(null);
    }

    /** 用户资质等级：STUDENT < (MAINTAINER / TEACHER) < ADMIN，用于最低资质校验 */
    private int qualRank(String userType) {
        if (userType == null) return 0;
        switch (userType) {
            case "STUDENT": return 1;
            case "MAINTAINER": return 2;
            case "TEACHER": return 2;
            case "ADMIN": return 3;
            default: return 0;
        }
    }

    @Transactional
    public Reservation create(Reservation r) {
        // —— 基础时间校验 ——
        if (r.getReservationDate() == null) throw new BusinessException("预约日期不能为空");
        if (r.getStartTime() == null || r.getEndTime() == null || !r.getEndTime().isAfter(r.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        LocalDate today = LocalDate.now();
        if (r.getReservationDate().isBefore(today)) {
            throw new BusinessException("不能预约过去的日期");
        }

        // —— 悲观锁锁定资源行，串行化对同一资源的并发预约（防超订）——
        if ("LABORATORY".equals(r.getResourceType())) {
            Laboratory lab = laboratoryRepository.findByIdForUpdate(r.getResourceId())
                    .orElseThrow(() -> new BusinessException("实验室不存在"));
            if (lab.getStatus() != null && lab.getStatus() == 0) {
                throw new BusinessException("该实验室当前不可预约");
            }
        } else if ("INSTRUMENT".equals(r.getResourceType())) {
            Instrument inst = instrumentRepository.findByIdForUpdate(r.getResourceId())
                    .orElseThrow(() -> new BusinessException("仪器不存在"));
            String st = inst.getStatus();
            if ("FAULT".equals(st) || "MAINTENANCE".equals(st) || "SCRAPPED".equals(st)) {
                throw new BusinessException("该仪器当前状态不可预约");
            }
        }

        // —— 预约规则引擎 ——
        ReservationRule rule = resolveRule(r.getResourceType(), r.getResourceId());
        if (rule != null) {
            // 提前预约天数：预约日期不得晚于 today + advanceDays
            if (rule.getAdvanceDays() != null && r.getReservationDate().isAfter(today.plusDays(rule.getAdvanceDays()))) {
                throw new BusinessException("最多可提前 " + rule.getAdvanceDays() + " 天预约");
            }
            // 单次最长使用时长
            if (rule.getMaxDurationHours() != null) {
                long minutes = Duration.between(r.getStartTime(), r.getEndTime()).toMinutes();
                if (minutes > rule.getMaxDurationHours() * 60L) {
                    throw new BusinessException("单次预约时长不能超过 " + rule.getMaxDurationHours() + " 小时");
                }
            }
            // 每日可预约次数
            if (rule.getMaxTimesPerDay() != null) {
                long used = reservationRepository.countActiveByUserResourceDate(
                        r.getUserId(), r.getResourceId(), r.getResourceType(), r.getReservationDate());
                if (used >= rule.getMaxTimesPerDay()) {
                    throw new BusinessException("该资源每天最多预约 " + rule.getMaxTimesPerDay() + " 次");
                }
            }
            // 最低资质要求
            if (rule.getMinQualification() != null && !rule.getMinQualification().isBlank()) {
                int need = qualRank(rule.getMinQualification());
                String userType = userRepository.findById(r.getUserId()).map(SysUser::getUserType).orElse(null);
                if (qualRank(userType) < need) {
                    throw new BusinessException("您的资质不满足该预约要求（需 " + rule.getMinQualification() + " 及以上）");
                }
            }
            // 可预约时间窗口
            if (rule.getAvailableStartTime() != null && rule.getAvailableEndTime() != null
                    && !rule.getAvailableStartTime().isBlank() && !rule.getAvailableEndTime().isBlank()) {
                LocalTime winStart = LocalTime.parse(rule.getAvailableStartTime());
                LocalTime winEnd = LocalTime.parse(rule.getAvailableEndTime());
                if (r.getStartTime().isBefore(winStart) || r.getEndTime().isAfter(winEnd)) {
                    throw new BusinessException("预约时间需在开放时段 "
                            + rule.getAvailableStartTime() + "~" + rule.getAvailableEndTime() + " 内");
                }
            }
        }

        // —— 实验室容量校验 ——
        if ("LABORATORY".equals(r.getResourceType()) && r.getParticipantCount() != null) {
            Laboratory lab = laboratoryRepository.findById(r.getResourceId()).orElse(null);
            if (lab != null && lab.getCapacity() != null && r.getParticipantCount() > lab.getCapacity()) {
                throw new BusinessException("参与人数超过实验室容量（" + lab.getCapacity() + "人）");
            }
        }

        // —— 时间冲突校验（同一资源同一日期时段重叠）——
        List<Reservation> conflicts = reservationRepository.findConflicting(r.getResourceId(), r.getResourceType(), r.getReservationDate());
        for (Reservation c : conflicts) {
            if (r.getStartTime().isBefore(c.getEndTime()) && r.getEndTime().isAfter(c.getStartTime())) {
                throw new BusinessException("该时段已被预约，存在时间冲突");
            }
        }

        r.setReservationNo(CommonUtils.generateOrderNo("RSV", 6));
        // —— 自动审批：命中规则且开启自动审批时直接置为已通过，并生成关联实验记录 ——
        boolean auto = rule != null && Boolean.TRUE.equals(rule.getAutoApprove());
        if (auto) {
            r.setStatus("APPROVED");
            r.setApproveTime(LocalDateTime.now());
            Reservation saved = reservationRepository.save(r);
            ensureLogForReservation(saved, "NOT_STARTED");
            return saved;
        }
        r.setStatus("PENDING");
        return reservationRepository.save(r);
    }

    @Transactional
    public Reservation approve(Long id, Long approverId) {
        Reservation r = getById(id);
        if (!"PENDING".equals(r.getStatus())) throw new BusinessException("只有待审批的预约才能审批通过");
        r.setStatus("APPROVED");
        r.setApproverId(approverId);
        r.setApproveTime(LocalDateTime.now());
        Reservation saved = reservationRepository.save(r);
        // 审批通过后自动创建关联的实验记录（未开始），保证预约与实验记录一一对应
        ensureLogForReservation(saved, "NOT_STARTED");
        return saved;
    }

    /**
     * 为预约确保存在一条关联的实验记录：不存在则新建，存在则按需更新状态。
     * 实验记录通过 reservationId 与预约一一绑定。
     */
    private ExperimentLog ensureLogForReservation(Reservation r, String logStatus) {
        ExperimentLog log = experimentLogRepository.findByReservationId(r.getId()).orElse(null);
        if (log == null) {
            log = new ExperimentLog();
            log.setReservationId(r.getId());
            log.setUserId(r.getUserId());
            // 实验室类型预约直接沿用资源ID作为实验室ID，其它资源类型留空
            if ("LABORATORY".equals(r.getResourceType())) log.setLaboratoryId(r.getResourceId());
            log.setExperimentTopic(r.getExperimentTopic());
        }
        log.setStatus(logStatus);
        return experimentLogRepository.save(log);
    }

    /** 预约状态 -> 实验记录状态 的映射，用于反向同步 */
    private String mapReservationToLogStatus(String reservationStatus) {
        switch (reservationStatus) {
            case "APPROVED": return "NOT_STARTED";
            case "IN_USE": return "IN_PROGRESS";
            case "COMPLETED": return "COMPLETED";
            case "CANCELLED":
            case "REJECTED": return "CANCELLED";
            default: return null; // PENDING 等状态不生成实验记录
        }
    }

    /**
     * 以预约为准，反向同步关联实验记录的状态与时间，保证两条数据一一对应。
     * 若映射后无需对应实验记录（如待审批），则不做处理。
     */
    private void syncLogStatusFromReservation(Reservation r) {
        String logStatus = mapReservationToLogStatus(r.getStatus());
        if (logStatus == null) return;
        ExperimentLog log = ensureLogForReservation(r, logStatus);
        log.setStartTime(r.getCheckInTime());
        log.setEndTime(r.getCheckOutTime());
        if (r.getCheckInTime() != null && r.getCheckOutTime() != null) {
            log.setActualDurationMinutes((int) java.time.Duration.between(r.getCheckInTime(), r.getCheckOutTime()).toMinutes());
        }
        experimentLogRepository.save(log);
    }

    @Transactional
    public Reservation reject(Long id, Long approverId, String reason) {
        Reservation r = getById(id);
        if (!"PENDING".equals(r.getStatus())) throw new BusinessException("只有待审批的预约才能驳回");
        r.setStatus("REJECTED");
        r.setApproverId(approverId);
        r.setRejectReason(reason);
        r.setApproveTime(LocalDateTime.now());
        Reservation saved = reservationRepository.save(r);
        syncLogStatusFromReservation(saved);
        return saved;
    }

    /** 校验操作者是否为预约本人（管理员 userId=1 可代为处理），否则抛 403 */
    private void assertOwner(Reservation r, Long currentUserId) {
        boolean owner = currentUserId != null && currentUserId.equals(r.getUserId());
        boolean admin = currentUserId != null && currentUserId == 1L;
        if (!owner && !admin) {
            throw new BusinessException(403, "只能操作本人的预约");
        }
    }

    @Transactional
    public Reservation cancel(Long id, Long currentUserId) {
        Reservation r = getById(id);
        assertOwner(r, currentUserId);
        String st = r.getStatus();
        if ("COMPLETED".equals(st) || "CANCELLED".equals(st) || "REJECTED".equals(st)) {
            throw new BusinessException("该预约已结束，无法取消");
        }
        r.setStatus("CANCELLED");
        Reservation saved = reservationRepository.save(r);
        syncLogStatusFromReservation(saved);
        return saved;
    }

    /**
     * 系统违约处理：定时任务调用。将「今天已过结束时间仍未签到」的已审批预约置为已取消，
     * 并反向同步关联实验记录。仅处理 APPROVED 状态，其它状态直接跳过返回 null。
     */
    @Transactional
    public Reservation markNoShow(Long id) {
        Reservation r = reservationRepository.findById(id).orElse(null);
        if (r == null || !"APPROVED".equals(r.getStatus())) return null;
        r.setStatus("CANCELLED");
        r.setRemark("系统自动取消：预约时段结束仍未签到（违约）");
        Reservation saved = reservationRepository.save(r);
        syncLogStatusFromReservation(saved);
        return saved;
    }

    /**
     * 签到：写签到时间，状态置为 IN_USE（使用中）。
     * 状态流转（论文表3-6）：APPROVED 已通过 -> IN_USE 使用中
     */
    @Transactional
    public Reservation checkIn(Long id, Long currentUserId) {
        Reservation r = getById(id);
        assertOwner(r, currentUserId);
        if (!"APPROVED".equals(r.getStatus())) throw new BusinessException("只有已通过的预约才能签到");
        r.setCheckInTime(LocalDateTime.now());
        r.setStatus("IN_USE");
        Reservation saved = reservationRepository.save(r);
        syncLogStatusFromReservation(saved);
        return saved;
    }

    /**
     * 签退：状态流转（论文表3-6）IN_USE 使用中 -> COMPLETED 已完成，同时写入签退时间。
     * 只有使用中的预约才能签退。
     */
    @Transactional
    public Reservation checkOut(Long id, Long currentUserId) {
        Reservation r = getById(id);
        assertOwner(r, currentUserId);
        if (!"IN_USE".equals(r.getStatus())) throw new BusinessException("只有使用中的预约才能签退");
        r.setCheckOutTime(LocalDateTime.now());
        r.setStatus("COMPLETED");
        Reservation saved = reservationRepository.save(r);
        syncLogStatusFromReservation(saved);
        return saved;
    }

    /** 按状态统计预约数量 */
    public long countByStatus(String status) { return reservationRepository.countByStatus(status); }
}
