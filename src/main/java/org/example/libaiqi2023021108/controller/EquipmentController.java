package org.example.libaiqi2023021108.controller;

import org.example.libaiqi2023021108.common.Result;
import org.example.libaiqi2023021108.dto.*;
import org.example.libaiqi2023021108.entity.*;
import org.example.libaiqi2023021108.service.EquipmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
/**
 * 模块：设备运维
 * 功能：设备运维
 * 权限：/repairs 故障报修 equipment:repair；/maintenance-* 保养 equipment:maintenance；
 *      /alerts 安全预警 equipment:alert；/hazardous-records 危化品管控 equipment:hazardous
 */

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {
    private final EquipmentService equipmentService;
    public EquipmentController(EquipmentService equipmentService) { this.equipmentService = equipmentService; }

    // ========== Repair Order ==========
    @GetMapping("/repairs")
    @PreAuthorize("hasAuthority('equipment:repair')")
    public Result<List<RepairOrderDTO>> listRepairs(@RequestParam(required = false) Long instrumentId, @RequestParam(required = false) Long assigneeId,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<RepairOrderDTO> p = equipmentService.listRepairsWithDetails(instrumentId, assigneeId, status, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }
    @GetMapping("/repairs/{id}")
    @PreAuthorize("hasAuthority('equipment:repair')")
    public Result<RepairOrderDTO> getRepairDetail(@PathVariable Long id) {
        return Result.success(equipmentService.getRepairDetail(id));
    }
    @PostMapping("/repairs")
    @PreAuthorize("hasAuthority('equipment:repair')")
    public Result<RepairOrder> createRepair(@RequestBody RepairOrder r) { return Result.success(equipmentService.createRepair(r)); }
    @PutMapping("/repairs/{id}/assign")
    @PreAuthorize("hasAuthority('equipment:repair')")
    public Result<RepairOrder> assignRepair(@PathVariable Long id, @RequestBody Map<String, Long> body) { return Result.success(equipmentService.assignRepair(id, body.get("assigneeId"))); }
    @PutMapping("/repairs/{id}/process")
    @PreAuthorize("hasAuthority('equipment:repair')")
    public Result<RepairOrder> processRepair(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String handleResult = body != null ? body.get("handleResult") : null;
        return Result.success(equipmentService.processRepair(id, handleResult));
    }
    @PutMapping("/repairs/{id}/complete")
    @PreAuthorize("hasAuthority('equipment:repair')")
    public Result<RepairOrder> completeRepair(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        String result = body != null ? (String) body.get("result") : null;
        BigDecimal cost = body != null && body.get("cost") != null ? new BigDecimal(body.get("cost").toString()) : null;
        return Result.success(equipmentService.completeRepair(id, result, cost));
    }

    // ========== Maintenance Plan ==========
    @GetMapping("/maintenance-plans")
    @PreAuthorize("hasAuthority('equipment:maintenance')")
    public Result<List<MaintenancePlanDTO>> listPlans(@RequestParam(required = false) Long instrumentId) {
        return Result.success(equipmentService.listPlansWithDetails(instrumentId));
    }
    @PostMapping("/maintenance-plans")
    @PreAuthorize("hasAuthority('equipment:maintenance')")
    public Result<MaintenancePlan> createPlan(@RequestBody MaintenancePlan p) { return Result.success(equipmentService.createPlan(p)); }
    @PutMapping("/maintenance-plans/{id}")
    @PreAuthorize("hasAuthority('equipment:maintenance')")
    public Result<MaintenancePlan> updatePlan(@PathVariable Long id, @RequestBody MaintenancePlan p) { return Result.success(equipmentService.updatePlan(id, p)); }
    @PostMapping("/maintenance-plans/{id}/execute")
    @PreAuthorize("hasAuthority('equipment:maintenance')")
    public Result<MaintenanceRecord> executePlan(@PathVariable Long id) { return Result.success(equipmentService.executePlan(id)); }

    // ========== Maintenance Record ==========
    @GetMapping("/maintenance-records")
    @PreAuthorize("hasAuthority('equipment:maintenance')")
    public Result<List<MaintenanceRecordDTO>> listRecords(@RequestParam(required = false) Long instrumentId, @RequestParam(required = false) Long maintainerId,
                                                           @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<MaintenanceRecordDTO> p = equipmentService.listRecordsWithDetails(instrumentId, maintainerId, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }
    @PostMapping("/maintenance-records")
    @PreAuthorize("hasAuthority('equipment:maintenance')")
    public Result<MaintenanceRecord> createRecord(@RequestBody MaintenanceRecord r) { return Result.success(equipmentService.createRecord(r)); }

    // ========== Safety Alert ==========
    @GetMapping("/alerts")
    @PreAuthorize("hasAuthority('equipment:alert')")
    public Result<List<SafetyAlert>> listAlerts(@RequestParam(required = false) String alertType, @RequestParam(required = false) String status,
                                                 @RequestParam(required = false) String alertLevel,
                                                 @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<SafetyAlert> p = equipmentService.listAlerts(alertType, status, alertLevel, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }
    @PostMapping("/alerts")
    @PreAuthorize("hasAuthority('equipment:alert')")
    public Result<SafetyAlert> createAlert(@RequestBody SafetyAlert a) { return Result.success(equipmentService.createAlert(a)); }
    @PutMapping("/alerts/{id}/resolve")
    @PreAuthorize("hasAuthority('equipment:alert')")
    public Result<SafetyAlert> resolveAlert(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        String resolveResult = body != null ? (String) body.get("resolveResult") : null;
        return Result.success(equipmentService.resolveAlert(id, resolveResult));
    }

    @PostMapping("/check-alerts")
    @PreAuthorize("hasAuthority('equipment:alert')")
    public Result<Void> checkAlerts() { equipmentService.checkAndGenerateAlerts(); return Result.success(); }

    // ========== Hazardous Material Record ==========
    @GetMapping("/hazardous-records")
    @PreAuthorize("hasAuthority('equipment:hazardous')")
    public Result<List<HazardousMaterialRecordDTO>> listHazardousRecords(@RequestParam(required = false) Long consumableId, @RequestParam(required = false) Long userId,
                                                                        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<HazardousMaterialRecordDTO> p = equipmentService.listHazardousRecordsWithDetails(consumableId, userId, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }
    @PostMapping("/hazardous-records")
    @PreAuthorize("hasAuthority('equipment:hazardous')")
    public Result<HazardousMaterialRecord> createHazardousRecord(@RequestBody HazardousMaterialRecord r) { return Result.success(equipmentService.createHazardousRecord(r)); }
}
