package org.example.libaiqi2023021108.service;

import org.example.libaiqi2023021108.common.BusinessException;
import org.example.libaiqi2023021108.common.CommonUtils;
import org.example.libaiqi2023021108.dto.ExperimentLogDTO;
import org.example.libaiqi2023021108.dto.InventoryCheckDTO;
import org.example.libaiqi2023021108.dto.MaterialRequisitionDTO;
import org.example.libaiqi2023021108.entity.*;
import org.example.libaiqi2023021108.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
/**
 * 模块：实验管理
 * 功能：实验管理服务
 */

@Service
public class ExperimentService {

    private final ExperimentLogRepository experimentLogRepository;
    private final ExperimentRecordRepository experimentRecordRepository;
    private final MaterialRequisitionRepository requisitionRepository;
    private final MaterialRequisitionItemRepository requisitionItemRepository;
    private final TemporaryRequisitionRepository tempRequisitionRepository;
    private final MaterialReturnRepository returnRepository;
    private final InventoryCheckRepository inventoryCheckRepository;
    private final ConsumableRepository consumableRepository;
    private final SysUserRepository userRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final ReservationRepository reservationRepository;

    public ExperimentService(ExperimentLogRepository experimentLogRepository,
                             ExperimentRecordRepository experimentRecordRepository,
                             MaterialRequisitionRepository requisitionRepository,
                             MaterialRequisitionItemRepository requisitionItemRepository,
                             TemporaryRequisitionRepository tempRequisitionRepository,
                             MaterialReturnRepository returnRepository,
                             InventoryCheckRepository inventoryCheckRepository,
                             ConsumableRepository consumableRepository,
                             SysUserRepository userRepository,
                             LaboratoryRepository laboratoryRepository,
                             ReservationRepository reservationRepository) {
        this.experimentLogRepository = experimentLogRepository;
        this.experimentRecordRepository = experimentRecordRepository;
        this.requisitionRepository = requisitionRepository;
        this.requisitionItemRepository = requisitionItemRepository;
        this.tempRequisitionRepository = tempRequisitionRepository;
        this.returnRepository = returnRepository;
        this.inventoryCheckRepository = inventoryCheckRepository;
        this.consumableRepository = consumableRepository;
        this.userRepository = userRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.reservationRepository = reservationRepository;
    }

    // ========== Experiment Log ==========
    public Page<ExperimentLogDTO> listLogsWithDetails(Long userId, Long labId, String status, Pageable pageable) {
        Page<ExperimentLog> page;
        if (userId != null) page = experimentLogRepository.findByUserId(userId, pageable);
        else if (labId != null) page = experimentLogRepository.findByLaboratoryId(labId, pageable);
        else if (status != null && !status.isBlank()) page = experimentLogRepository.findByStatus(status, pageable);
        else page = experimentLogRepository.findAll(pageable);
        return page.map(this::convertLogToDTO);
    }

    public ExperimentLog findLogById(Long id) {
        return experimentLogRepository.findById(id).orElse(null);
    }

    @Transactional
    public ExperimentLog updateLog(Long id, ExperimentLog log) {
        ExperimentLog existing = experimentLogRepository.findById(id).orElseThrow(() -> new BusinessException("实验记录不存在"));
        String st = existing.getStatus();
        if ("COMPLETED".equals(st) || "CANCELLED".equals(st)) {
            throw new BusinessException("已完成或已取消的实验记录不可编辑");
        }
        if (log.getExperimentTopic() != null) existing.setExperimentTopic(log.getExperimentTopic());
        if (log.getStartTime() != null) existing.setStartTime(log.getStartTime());
        if (log.getEndTime() != null) existing.setEndTime(log.getEndTime());
        if (log.getStatus() != null) existing.setStatus(log.getStatus());
        return experimentLogRepository.save(existing);
    }

    @Transactional
    public void deleteLog(Long id) {
        ExperimentLog existing = experimentLogRepository.findById(id).orElseThrow(() -> new BusinessException("实验记录不存在"));
        String st = existing.getStatus();
        if ("COMPLETED".equals(st) || "CANCELLED".equals(st)) {
            throw new BusinessException("已完成或已取消的实验记录不可删除");
        }
        experimentLogRepository.deleteById(id);
    }

    @Transactional
    public ExperimentLog checkInLog(Long id) {
        ExperimentLog log = experimentLogRepository.findById(id).orElseThrow(() -> new BusinessException("实验记录不存在"));
        if (!"NOT_STARTED".equals(log.getStatus())) throw new BusinessException("只有未开始的实验记录才能签到");
        log.setStatus("IN_PROGRESS");
        log.setStartTime(LocalDateTime.now());
        if (log.getQrCode() == null || log.getQrCode().isBlank()) {
            log.setQrCode("QR-" + UUID.randomUUID().toString().substring(0, 8));
        }
        ExperimentLog saved = experimentLogRepository.save(log);
        syncReservationFromLog(saved);
        return saved;
    }

    /** 取消实验记录：仅未开始/进行中可取消，取消后同步关联预约 */
    @Transactional
    public ExperimentLog cancelLog(Long id) {
        ExperimentLog log = experimentLogRepository.findById(id).orElseThrow(() -> new BusinessException("实验记录不存在"));
        String st = log.getStatus();
        if ("COMPLETED".equals(st) || "CANCELLED".equals(st)) {
            throw new BusinessException("已完成或已取消的实验记录不可取消");
        }
        log.setStatus("CANCELLED");
        ExperimentLog saved = experimentLogRepository.save(log);
        syncReservationFromLog(saved);
        return saved;
    }

    /**
     * 以实验记录为准，反向同步关联预约的状态与时间，保证两条数据一一对应。
     * 记录签到 -> 预约 APPROVED 转 IN_USE；记录签退 -> 预约转 COMPLETED；记录取消 -> 预约转 CANCELLED。
     */
    private void syncReservationFromLog(ExperimentLog log) {
        if (log.getReservationId() == null) return;
        Reservation r = reservationRepository.findById(log.getReservationId()).orElse(null);
        if (r == null) return;
        String rs = r.getStatus();
        String ls = log.getStatus();
        if ("IN_PROGRESS".equals(ls)) {
            if ("APPROVED".equals(rs)) {
                r.setStatus("IN_USE");
                if (r.getCheckInTime() == null) r.setCheckInTime(log.getStartTime() != null ? log.getStartTime() : LocalDateTime.now());
                reservationRepository.save(r);
            }
        } else if ("COMPLETED".equals(ls)) {
            if ("IN_USE".equals(rs) || "APPROVED".equals(rs)) {
                if (r.getCheckInTime() == null) r.setCheckInTime(log.getStartTime());
                if (r.getCheckOutTime() == null) r.setCheckOutTime(log.getEndTime() != null ? log.getEndTime() : LocalDateTime.now());
                r.setStatus("COMPLETED");
                reservationRepository.save(r);
            }
        } else if ("CANCELLED".equals(ls)) {
            if (!"COMPLETED".equals(rs) && !"REJECTED".equals(rs) && !"CANCELLED".equals(rs)) {
                r.setStatus("CANCELLED");
                reservationRepository.save(r);
            }
        }
    }

    private ExperimentLogDTO convertLogToDTO(ExperimentLog log) {
        ExperimentLogDTO dto = new ExperimentLogDTO();
        dto.setId(log.getId());
        dto.setReservationId(log.getReservationId());
        dto.setUserId(log.getUserId());
        dto.setLaboratoryId(log.getLaboratoryId());
        dto.setExperimentTopic(log.getExperimentTopic());
        dto.setStartTime(log.getStartTime());
        dto.setEndTime(log.getEndTime());
        dto.setActualDurationMinutes(log.getActualDurationMinutes());
        dto.setStatus(log.getStatus());
        dto.setQrCode(log.getQrCode());
        dto.setCreateTime(log.getCreateTime());
        dto.setRemark(log.getRemark());
        userRepository.findById(log.getUserId()).ifPresent(u ->
            dto.setUserName(u.getRealName() != null ? u.getRealName() : u.getUsername())
        );
        if (log.getLaboratoryId() != null) {
            laboratoryRepository.findById(log.getLaboratoryId()).ifPresent(lab ->
                dto.setLaboratoryName(lab.getLabName())
            );
        }
        return dto;
    }

    @Transactional
    public ExperimentLog checkOut(Long logId) {
        ExperimentLog log = experimentLogRepository.findById(logId).orElseThrow(() -> new BusinessException("实验记录不存在"));
        if (!"IN_PROGRESS".equals(log.getStatus())) throw new BusinessException("只有进行中的实验记录才能签退");
        log.setEndTime(LocalDateTime.now()); log.setStatus("COMPLETED");
        if (log.getStartTime() != null) {
            log.setActualDurationMinutes((int) java.time.Duration.between(log.getStartTime(), log.getEndTime()).toMinutes());
        }
        ExperimentLog saved = experimentLogRepository.save(log);
        syncReservationFromLog(saved);
        return saved;
    }

    // ========== Experiment Record ==========
    public Page<ExperimentRecord> listRecords(Long logId, Long userId, Pageable pageable) {
        if (logId != null) return experimentRecordRepository.findByExperimentLogId(logId, pageable);
        if (userId != null) return experimentRecordRepository.findByUserId(userId, pageable);
        return experimentRecordRepository.findAll(pageable);
    }
    public ExperimentRecord createRecord(ExperimentRecord r) { return experimentRecordRepository.save(r); }

    // ========== Material Requisition ==========
    public Page<MaterialRequisitionDTO> listRequisitionsWithDetails(Long userId, String status, Pageable pageable) {
        Page<MaterialRequisition> page;
        if (userId != null) page = requisitionRepository.findByUserId(userId, pageable);
        else if (status != null && !status.isBlank()) page = requisitionRepository.findByStatus(status, pageable);
        else page = requisitionRepository.findAll(pageable);
        return page.map(this::convertToDTO);
    }

    /** 查询领用单详情（含明细项和耗材信息） */
    public MaterialRequisitionDTO getRequisitionDetail(Long id) {
        MaterialRequisition r = requisitionRepository.findById(id).orElseThrow(() -> new BusinessException("领用单不存在"));
        return convertToDTO(r);
    }

    /** 将领用单转换为DTO，补充申请人姓名、实验室名称和明细项 */
    private MaterialRequisitionDTO convertToDTO(MaterialRequisition r) {
        MaterialRequisitionDTO dto = new MaterialRequisitionDTO();
        dto.setId(r.getId());
        dto.setRequisitionNo(r.getRequisitionNo());
        dto.setUserId(r.getUserId());
        dto.setExperimentLogId(r.getExperimentLogId());
        dto.setExperimentProject(r.getExperimentProject());
        dto.setStatus(r.getStatus());
        dto.setApproverId(r.getApproverId());
        dto.setApproveTime(r.getApproveTime());
        dto.setTotalItems(r.getTotalItems());
        dto.setApplyTime(r.getCreateTime());

        userRepository.findById(r.getUserId()).ifPresent(user ->
            dto.setApplicantName(user.getRealName() != null ? user.getRealName() : user.getUsername())
        );

        if (r.getExperimentLogId() != null) {
            experimentLogRepository.findById(r.getExperimentLogId()).ifPresent(log -> {
                if (log.getLaboratoryId() != null) {
                    laboratoryRepository.findById(log.getLaboratoryId()).ifPresent(lab ->
                        dto.setLaboratoryName(lab.getLabName())
                    );
                }
                dto.setLaboratoryId(log.getLaboratoryId());
            });
        }

        List<MaterialRequisitionItem> items = requisitionItemRepository.findByRequisitionId(r.getId());
        List<MaterialRequisitionDTO.MaterialRequisitionItemDTO> itemDTOs = items.stream().map(item -> {
            MaterialRequisitionDTO.MaterialRequisitionItemDTO itemDTO = new MaterialRequisitionDTO.MaterialRequisitionItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setRequisitionId(item.getRequisitionId());
            itemDTO.setConsumableId(item.getConsumableId());
            itemDTO.setRequestQuantity(item.getRequestQuantity());
            itemDTO.setActualQuantity(item.getActualQuantity());

            consumableRepository.findById(item.getConsumableId()).ifPresent(consumable -> {
                itemDTO.setConsumableName(consumable.getConsumableName());
                itemDTO.setUnit(consumable.getUnit());
                itemDTO.setSpecification(consumable.getSpecification());
            });

            return itemDTO;
        }).collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }

    @Transactional
    public MaterialRequisition createRequisition(MaterialRequisition r, List<MaterialRequisitionItem> items) {
        r.setRequisitionNo(CommonUtils.generateOrderNo("MR"));
        r.setTotalItems(items != null ? items.size() : 0);
        MaterialRequisition saved = requisitionRepository.save(r);
        if (items != null) {
            for (MaterialRequisitionItem item : items) {
                item.setRequisitionId(saved.getId());
                requisitionItemRepository.save(item);
            }
        }
        return saved;
    }

    public List<MaterialRequisitionItem> getRequisitionItems(Long requisitionId) {
        return requisitionItemRepository.findByRequisitionId(requisitionId);
    }

    @Transactional
    public MaterialRequisition approveRequisition(Long id, Long approverId) {
        MaterialRequisition r = requisitionRepository.findById(id).orElseThrow(() -> new BusinessException("领用单不存在"));
        if (!"PENDING".equals(r.getStatus())) throw new BusinessException("只有待审批的领用单才能审批通过");
        r.setStatus("APPROVED"); r.setApproverId(approverId); r.setApproveTime(LocalDateTime.now());
        List<MaterialRequisitionItem> items = requisitionItemRepository.findByRequisitionId(id);
        for (MaterialRequisitionItem item : items) {
            // 悲观锁锁定耗材行，防止并发扣减导致库存为负
            Consumable c = consumableRepository.findByIdForUpdate(item.getConsumableId()).orElse(null);
            if (c == null) throw new BusinessException("耗材不存在，无法审批领用");
            int req = item.getRequestQuantity() == null ? 0 : item.getRequestQuantity();
            if (c.getCurrentStock() == null || c.getCurrentStock() < req) {
                throw new BusinessException("耗材【" + c.getConsumableName() + "】库存不足，无法审批");
            }
            c.setCurrentStock(c.getCurrentStock() - req);
            consumableRepository.save(c);
            item.setActualQuantity(req);
            requisitionItemRepository.save(item);
        }
        r.setStatus("ISSUED");
        return requisitionRepository.save(r);
    }

    @Transactional
    public MaterialRequisition rejectRequisition(Long id) {
        MaterialRequisition r = requisitionRepository.findById(id).orElseThrow(() -> new BusinessException("领用单不存在"));
        r.setStatus("REJECTED");
        return requisitionRepository.save(r);
    }

    @Transactional
    public MaterialRequisition collectRequisition(Long id) {
        MaterialRequisition r = requisitionRepository.findById(id).orElseThrow(() -> new BusinessException("领用单不存在"));
        if (!"APPROVED".equals(r.getStatus()) && !"ISSUED".equals(r.getStatus())) {
            throw new BusinessException("只有已批准的领用单才能确认领取");
        }
        r.setStatus("COLLECTED");
        return requisitionRepository.save(r);
    }

    // ========== Temporary Requisition ==========
    public Page<TemporaryRequisition> listTempRequisitions(Long userId, String status, Pageable pageable) {
        if (userId != null) return tempRequisitionRepository.findByUserId(userId, pageable);
        if (status != null && !status.isBlank()) return tempRequisitionRepository.findByStatus(status, pageable);
        return tempRequisitionRepository.findAll(pageable);
    }

    @Transactional
    public TemporaryRequisition createTempRequisition(TemporaryRequisition r) {
        r.setRequisitionNo(CommonUtils.generateOrderNo("TR"));
        return tempRequisitionRepository.save(r);
    }

    // ========== Material Return ==========
    public Page<MaterialReturn> listReturns(Long userId, String status, Pageable pageable) {
        if (userId != null) return returnRepository.findByUserId(userId, pageable);
        if (status != null && !status.isBlank()) return returnRepository.findByStatus(status, pageable);
        return returnRepository.findAll(pageable);
    }

    @Transactional
    public MaterialReturn createReturn(MaterialReturn r) {
        r.setReturnNo(CommonUtils.generateOrderNo("RT"));
        MaterialReturn saved = returnRepository.save(r);
        Consumable c = consumableRepository.findById(r.getConsumableId()).orElse(null);
        if (c != null) { c.setCurrentStock(c.getCurrentStock() + r.getReturnQuantity()); consumableRepository.save(c); }
        return saved;
    }

    // ========== Inventory Check ==========
    public Page<InventoryCheckDTO> listChecksWithDetails(Long consumableId, String status, Pageable pageable) {
        Page<InventoryCheck> page;
        if (consumableId != null) page = inventoryCheckRepository.findByConsumableId(consumableId, pageable);
        else if (status != null && !status.isBlank()) page = inventoryCheckRepository.findByStatus(status, pageable);
        else page = inventoryCheckRepository.findAll(pageable);
        return page.map(this::convertCheckToDTO);
    }

    private InventoryCheckDTO convertCheckToDTO(InventoryCheck c) {
        InventoryCheckDTO dto = new InventoryCheckDTO();
        dto.setId(c.getId());
        dto.setCheckNo(c.getCheckNo());
        dto.setCheckName(c.getCheckName());
        dto.setCheckDate(c.getCheckDate());
        dto.setCheckerId(c.getCheckerId());
        dto.setConsumableId(c.getConsumableId());
        dto.setSystemQuantity(c.getSystemQuantity());
        dto.setActualQuantity(c.getActualQuantity());
        dto.setDifference(c.getDifference());
        dto.setLossReason(c.getLossReason());
        dto.setStatus(c.getStatus());
        dto.setRemark(c.getRemark());
        dto.setCreateTime(c.getCreateTime());
        if (c.getCheckerId() != null) {
            userRepository.findById(c.getCheckerId()).ifPresent(u ->
                dto.setCheckerName(u.getRealName() != null ? u.getRealName() : u.getUsername())
            );
        }
        if (c.getConsumableId() != null) {
            consumableRepository.findById(c.getConsumableId()).ifPresent(consumable ->
                dto.setConsumableName(consumable.getConsumableName())
            );
        }
        return dto;
    }

    @Transactional
    public InventoryCheck createCheck(InventoryCheck c) {
        c.setCheckNo(CommonUtils.generateOrderNo("IC"));
        if (c.getSystemQuantity() != null && c.getActualQuantity() != null) {
            c.setDifference(c.getActualQuantity() - c.getSystemQuantity());
        }
        return inventoryCheckRepository.save(c);
    }

    @Transactional
    public InventoryCheck completeCheck(Long id) {
        InventoryCheck c = inventoryCheckRepository.findById(id).orElseThrow(() -> new BusinessException("盘点记录不存在"));
        c.setStatus("COMPLETED");
        if (c.getSystemQuantity() != null && c.getActualQuantity() != null) {
            c.setDifference(c.getActualQuantity() - c.getSystemQuantity());
        }
        return inventoryCheckRepository.save(c);
    }

    public List<InventoryCheckDTO> getCheckItems(Long id) {
        return inventoryCheckRepository.findById(id)
                .map(c -> java.util.Collections.singletonList(convertCheckToDTO(c)))
                .orElse(java.util.Collections.emptyList());
    }
}
