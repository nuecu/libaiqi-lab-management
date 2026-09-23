package org.example.libaiqi2023021108.service;

import org.example.libaiqi2023021108.common.BusinessException;
import org.example.libaiqi2023021108.common.CommonUtils;
import org.example.libaiqi2023021108.dto.*;
import org.example.libaiqi2023021108.entity.*;
import org.example.libaiqi2023021108.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
/**
 * 模块：设备运维
 * 功能：设备运维服务
 */

@Service
public class EquipmentService {

    private final RepairOrderRepository repairOrderRepository;
    private final MaintenancePlanRepository maintenancePlanRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final SafetyAlertRepository safetyAlertRepository;
    private final HazardousMaterialRecordRepository hazardousRecordRepository;
    private final InstrumentRepository instrumentRepository;
    private final ConsumableRepository consumableRepository;
    private final SysUserRepository userRepository;

    public EquipmentService(RepairOrderRepository repairOrderRepository,
                            MaintenancePlanRepository maintenancePlanRepository,
                            MaintenanceRecordRepository maintenanceRecordRepository,
                            SafetyAlertRepository safetyAlertRepository,
                            HazardousMaterialRecordRepository hazardousRecordRepository,
                            InstrumentRepository instrumentRepository,
                            ConsumableRepository consumableRepository,
                            SysUserRepository userRepository) {
        this.repairOrderRepository = repairOrderRepository;
        this.maintenancePlanRepository = maintenancePlanRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.safetyAlertRepository = safetyAlertRepository;
        this.hazardousRecordRepository = hazardousRecordRepository;
        this.instrumentRepository = instrumentRepository;
        this.consumableRepository = consumableRepository;
        this.userRepository = userRepository;
    }

    // ========== Repair Order ==========
    /** 查询报修工单列表（DTO格式，含报修人/处理人姓名） */
    public Page<RepairOrderDTO> listRepairsWithDetails(Long instrumentId, Long assigneeId, String status, Pageable pageable) {
        Page<RepairOrder> page;
        if (instrumentId != null) page = repairOrderRepository.findByInstrumentId(instrumentId, pageable);
        else if (assigneeId != null) page = repairOrderRepository.findByAssigneeId(assigneeId, pageable);
        else if (status != null && !status.isBlank()) page = repairOrderRepository.findByStatus(status, pageable);
        else page = repairOrderRepository.findAll(pageable);
        return page.map(this::convertRepairToDTO);
    }

    public RepairOrderDTO getRepairDetail(Long id) {
        RepairOrder r = repairOrderRepository.findById(id).orElseThrow(() -> new BusinessException("报修单不存在"));
        return convertRepairToDTO(r);
    }

    /** 将报修工单转换为DTO，补充报修人和处理人姓名 */
    private RepairOrderDTO convertRepairToDTO(RepairOrder r) {
        RepairOrderDTO dto = new RepairOrderDTO();
        dto.setId(r.getId());
        dto.setOrderNo(r.getOrderNo());
        dto.setInstrumentId(r.getInstrumentId());
        dto.setInstrumentName(r.getInstrumentName());
        dto.setReporterId(r.getReporterId());
        dto.setFaultDesc(r.getFaultDesc());
        dto.setFaultImage(r.getFaultImage());
        dto.setAssigneeId(r.getAssigneeId());
        dto.setRepairDesc(r.getRepairDesc());
        dto.setRepairResult(r.getRepairResult());
        dto.setRepairCost(r.getRepairCost());
        dto.setStatus(r.getStatus());
        dto.setCompleteTime(r.getCompleteTime());
        dto.setReportTime(r.getCreateTime());
        dto.setRemark(r.getRemark());
        dto.setCreateTime(r.getCreateTime());
        userRepository.findById(r.getReporterId()).ifPresent(u ->
            dto.setReporterName(u.getRealName() != null ? u.getRealName() : u.getUsername())
        );
        if (r.getAssigneeId() != null) {
            userRepository.findById(r.getAssigneeId()).ifPresent(u ->
                dto.setHandlerName(u.getRealName() != null ? u.getRealName() : u.getUsername())
            );
        }
        return dto;
    }

    @Transactional
    public RepairOrder createRepair(RepairOrder r) {
        r.setOrderNo(CommonUtils.generateOrderNo("RP"));
        if (r.getReporterId() == null) r.setReporterId(1L);
        Instrument inst = instrumentRepository.findById(r.getInstrumentId()).orElse(null);
        if (inst != null) { r.setInstrumentName(inst.getInstrumentName()); inst.setStatus("FAULT"); instrumentRepository.save(inst); }
        return repairOrderRepository.save(r);
    }

    @Transactional
    public RepairOrder assignRepair(Long id, Long assigneeId) {
        RepairOrder r = repairOrderRepository.findById(id).orElseThrow(() -> new BusinessException("报修单不存在"));
        r.setAssigneeId(assigneeId); r.setStatus("ASSIGNED");
        return repairOrderRepository.save(r);
    }

    @Transactional
    public RepairOrder processRepair(Long id, String handleResult) {
        RepairOrder r = repairOrderRepository.findById(id).orElseThrow(() -> new BusinessException("报修单不存在"));
        r.setStatus("REPAIRING");
        r.setRepairDesc(handleResult);
        return repairOrderRepository.save(r);
    }

    @Transactional
    public RepairOrder completeRepair(Long id, String result, java.math.BigDecimal cost) {
        RepairOrder r = repairOrderRepository.findById(id).orElseThrow(() -> new BusinessException("报修单不存在"));
        r.setRepairResult(result); r.setRepairCost(cost); r.setStatus("COMPLETED"); r.setCompleteTime(LocalDateTime.now());
        Instrument inst = instrumentRepository.findById(r.getInstrumentId()).orElse(null);
        if (inst != null) { inst.setStatus("IDLE"); instrumentRepository.save(inst); }
        return repairOrderRepository.save(r);
    }

    // ========== Maintenance Plan ==========
    public List<MaintenancePlanDTO> listPlansWithDetails(Long instrumentId) {
        List<MaintenancePlan> plans = instrumentId != null
                ? maintenancePlanRepository.findByInstrumentId(instrumentId)
                : maintenancePlanRepository.findAll();
        return plans.stream().map(this::convertPlanToDTO).toList();
    }

    private MaintenancePlanDTO convertPlanToDTO(MaintenancePlan p) {
        MaintenancePlanDTO dto = new MaintenancePlanDTO();
        dto.setId(p.getId());
        dto.setPlanName(p.getPlanName());
        dto.setInstrumentId(p.getInstrumentId());
        dto.setMaintenanceType(p.getMaintenanceType());
        dto.setCycleDays(p.getCycleDays());
        dto.setLastMaintenanceDate(p.getLastMaintenanceDate());
        dto.setNextMaintenanceDate(p.getNextMaintenanceDate());
        dto.setResponsibleId(p.getResponsibleId());
        dto.setStatus(p.getStatus());
        dto.setEnabled("ACTIVE".equals(p.getStatus()));
        dto.setRemark(p.getRemark());
        dto.setCreateTime(p.getCreateTime());
        instrumentRepository.findById(p.getInstrumentId()).ifPresent(inst ->
            dto.setInstrumentName(inst.getInstrumentName())
        );
        return dto;
    }

    public MaintenancePlan createPlan(MaintenancePlan p) { return maintenancePlanRepository.save(p); }

    public MaintenancePlan updatePlan(Long id, MaintenancePlan p) {
        MaintenancePlan ex = maintenancePlanRepository.findById(id).orElseThrow(() -> new BusinessException("保养计划不存在"));
        ex.setPlanName(p.getPlanName()); ex.setMaintenanceType(p.getMaintenanceType());
        ex.setCycleDays(p.getCycleDays()); ex.setNextMaintenanceDate(p.getNextMaintenanceDate());
        ex.setResponsibleId(p.getResponsibleId()); ex.setStatus(p.getStatus());
        return maintenancePlanRepository.save(ex);
    }

    @Transactional
    public MaintenanceRecord executePlan(Long planId) {
        MaintenancePlan plan = maintenancePlanRepository.findById(planId).orElseThrow(() -> new BusinessException("保养计划不存在"));
        MaintenanceRecord record = new MaintenanceRecord();
        record.setPlanId(plan.getId());
        record.setInstrumentId(plan.getInstrumentId());
        record.setMaintenanceDate(LocalDate.now());
        record.setResult("保养执行完成");
        if (plan.getCycleDays() != null) {
            record.setNextMaintenanceDate(LocalDate.now().plusDays(plan.getCycleDays()));
        }
        MaintenanceRecord saved = maintenanceRecordRepository.save(record);
        plan.setLastMaintenanceDate(LocalDate.now());
        if (record.getNextMaintenanceDate() != null) plan.setNextMaintenanceDate(record.getNextMaintenanceDate());
        maintenancePlanRepository.save(plan);
        return saved;
    }

    // ========== Maintenance Record ==========
    public Page<MaintenanceRecordDTO> listRecordsWithDetails(Long instrumentId, Long maintainerId, Pageable pageable) {
        Page<MaintenanceRecord> page = maintainerId != null
                ? maintenanceRecordRepository.findByMaintainerId(maintainerId, pageable)
                : maintenanceRecordRepository.findAll(pageable);
        return page.map(this::convertRecordToDTO);
    }

    private MaintenanceRecordDTO convertRecordToDTO(MaintenanceRecord r) {
        MaintenanceRecordDTO dto = new MaintenanceRecordDTO();
        dto.setId(r.getId());
        dto.setPlanId(r.getPlanId());
        dto.setInstrumentId(r.getInstrumentId());
        dto.setMaintenanceDate(r.getMaintenanceDate());
        dto.setMaintainerId(r.getMaintainerId());
        dto.setMaintenanceContent(r.getMaintenanceContent());
        dto.setResult(r.getResult());
        dto.setNextMaintenanceDate(r.getNextMaintenanceDate());
        dto.setRemark(r.getRemark());
        dto.setCreateTime(r.getCreateTime());
        dto.setStatus("COMPLETED");
        instrumentRepository.findById(r.getInstrumentId()).ifPresent(inst ->
            dto.setInstrumentName(inst.getInstrumentName())
        );
        if (r.getPlanId() != null) {
            maintenancePlanRepository.findById(r.getPlanId()).ifPresent(plan ->
                dto.setPlanName(plan.getPlanName())
            );
        }
        if (r.getMaintainerId() != null) {
            userRepository.findById(r.getMaintainerId()).ifPresent(u ->
                dto.setExecutorName(u.getRealName() != null ? u.getRealName() : u.getUsername())
            );
        }
        return dto;
    }

    @Transactional
    public MaintenanceRecord createRecord(MaintenanceRecord r) {
        MaintenanceRecord saved = maintenanceRecordRepository.save(r);
        if (r.getPlanId() != null) {
            MaintenancePlan plan = maintenancePlanRepository.findById(r.getPlanId()).orElse(null);
            if (plan != null) {
                plan.setLastMaintenanceDate(r.getMaintenanceDate());
                if (r.getNextMaintenanceDate() != null) plan.setNextMaintenanceDate(r.getNextMaintenanceDate());
                else if (plan.getCycleDays() != null) plan.setNextMaintenanceDate(r.getMaintenanceDate().plusDays(plan.getCycleDays()));
                maintenancePlanRepository.save(plan);
            }
        }
        return saved;
    }

    // ========== Safety Alert ==========
    public Page<SafetyAlert> listAlerts(String alertType, String status, String alertLevel, Pageable pageable) {
        if (alertType != null) return safetyAlertRepository.findByAlertType(alertType, pageable);
        if (status != null && !status.isBlank()) return safetyAlertRepository.findByStatus(status, pageable);
        if (alertLevel != null) return safetyAlertRepository.findByAlertLevel(alertLevel, pageable);
        return safetyAlertRepository.findAll(pageable);
    }

    public SafetyAlert createAlert(SafetyAlert a) { return safetyAlertRepository.save(a); }

    @Transactional
    public SafetyAlert resolveAlert(Long id, String resolveResult) {
        SafetyAlert a = safetyAlertRepository.findById(id).orElseThrow(() -> new BusinessException("预警不存在"));
        a.setStatus("RESOLVED");
        a.setResolveTime(LocalDateTime.now());
        a.setRemark(resolveResult);
        return safetyAlertRepository.save(a);
    }

    /** 自动检查并生成预警：检测低库存耗材和逾期保养计划 */
    public void checkAndGenerateAlerts() {
        List<Consumable> lowStock = consumableRepository.findLowStockItems();
        for (Consumable c : lowStock) {
            SafetyAlert alert = new SafetyAlert();
            alert.setAlertType("INVENTORY_LOW"); alert.setAlertLevel("MEDIUM");
            alert.setAlertContent("耗材[" + c.getConsumableName() + "]库存不足，当前:" + c.getCurrentStock() + "，阈值:" + c.getMinStock());
            alert.setRelatedId(c.getId()); alert.setRelatedType("CONSUMABLE");
            safetyAlertRepository.save(alert);
        }
        List<MaintenancePlan> overdue = maintenancePlanRepository.findOverdue(LocalDate.now());
        for (MaintenancePlan p : overdue) {
            SafetyAlert alert = new SafetyAlert();
            alert.setAlertType("INSTRUMENT_OVERTIME"); alert.setAlertLevel("HIGH");
            alert.setAlertContent("仪器保养计划[" + p.getPlanName() + "]已逾期");
            alert.setRelatedId(p.getId()); alert.setRelatedType("MAINTENANCE_PLAN");
            safetyAlertRepository.save(alert);
        }
    }

    // ========== Hazardous Material Record ==========
    public Page<HazardousMaterialRecordDTO> listHazardousRecordsWithDetails(Long consumableId, Long userId, Pageable pageable) {
        Page<HazardousMaterialRecord> page;
        if (consumableId != null) page = hazardousRecordRepository.findByConsumableId(consumableId, pageable);
        else if (userId != null) page = hazardousRecordRepository.findByUserId(userId, pageable);
        else page = hazardousRecordRepository.findAll(pageable);
        return page.map(this::convertToDTO);
    }

    private HazardousMaterialRecordDTO convertToDTO(HazardousMaterialRecord r) {
        HazardousMaterialRecordDTO dto = new HazardousMaterialRecordDTO();
        dto.setId(r.getId());
        dto.setConsumableId(r.getConsumableId());
        dto.setMaterialName(r.getConsumableName());
        dto.setUserId(r.getUserId());
        dto.setOperationType(r.getOperationType());
        dto.setQuantity(r.getQuantity());
        dto.setDualConfirm(r.getDualConfirm());
        dto.setConfirmUser1Id(r.getConfirmUser1Id());
        dto.setConfirmUser2Id(r.getConfirmUser2Id());
        dto.setQualificationVerified(r.getQualificationVerified());
        dto.setUsageTracking(r.getUsageTracking());
        dto.setOperationTime(r.getCreateTime());

        userRepository.findById(r.getUserId()).ifPresent(user ->
            dto.setOperatorName(user.getRealName() != null ? user.getRealName() : user.getUsername())
        );

        consumableRepository.findById(r.getConsumableId()).ifPresent(c -> {
            if (dto.getMaterialName() == null) dto.setMaterialName(c.getConsumableName());
            dto.setUnit(c.getUnit());
            dto.setStorageLocation(c.getStorageLocation());
        });

        return dto;
    }

    @Transactional
    public HazardousMaterialRecord createHazardousRecord(HazardousMaterialRecord r) {
        if (r.getUserId() == null) r.setUserId(1L);
        consumableRepository.findById(r.getConsumableId()).ifPresent(c -> {
            if (r.getConsumableName() == null) r.setConsumableName(c.getConsumableName());
        });
        return hazardousRecordRepository.save(r);
    }
}
