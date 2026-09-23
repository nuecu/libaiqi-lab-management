package org.example.libaiqi2023021108.controller;

import org.example.libaiqi2023021108.common.BusinessException;
import org.example.libaiqi2023021108.common.Result;
import org.example.libaiqi2023021108.common.SecurityUtils;
import org.example.libaiqi2023021108.dto.ExperimentLogDTO;
import org.example.libaiqi2023021108.dto.InventoryCheckDTO;
import org.example.libaiqi2023021108.dto.MaterialRequisitionDTO;
import org.example.libaiqi2023021108.entity.*;
import org.example.libaiqi2023021108.repository.SysRoleRepository;
import org.example.libaiqi2023021108.repository.SysUserRoleRepository;
import org.example.libaiqi2023021108.service.ExperimentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
/**
 * 模块：实验管理
 * 功能：实验管理
 * 权限：/logs 实验记录需要 experiment:log；/requisitions、/temp-requisitions、/returns
 *      耗材领用归还需要 experiment:material；/inventory-checks 库存盘点需要 experiment:inventory
 */

@RestController
@RequestMapping("/api/experiments")
public class ExperimentController {
    private final ExperimentService experimentService;
    private final SysUserRoleRepository userRoleRepository;
    private final SysRoleRepository roleRepository;

    public ExperimentController(ExperimentService experimentService,
                                SysUserRoleRepository userRoleRepository,
                                SysRoleRepository roleRepository) {
        this.experimentService = experimentService;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }

    /** 检查当前用户是否为学生角色，学生无权修改实验记录 */
    private void checkNotStudent() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.valueOf(principal.toString());
        List<SysUserRole> userRoles = userRoleRepository.findByUserId(userId);
        for (SysUserRole ur : userRoles) {
            roleRepository.findById(ur.getRoleId()).ifPresent(role -> {
                if ("STUDENT".equals(role.getRoleCode())) {
                    throw new BusinessException(403, "学生无权修改实验记录，请联系教师或管理员");
                }
            });
        }
    }

    /** 当前登录用户是否为管理员 */
    private boolean isAdmin() {
        Long userId = SecurityUtils.currentUserId();
        for (SysUserRole ur : userRoleRepository.findByUserId(userId)) {
            if (roleRepository.findById(ur.getRoleId())
                    .map(r -> "ADMIN".equals(r.getRoleCode())).orElse(false)) {
                return true;
            }
        }
        return false;
    }

    // ========== Experiment Log ==========
    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('experiment:log')")
    public Result<List<ExperimentLogDTO>> listLogs(@RequestParam(required = false) Long userId, @RequestParam(required = false) Long labId,
                                                    @RequestParam(required = false) String status,
                                                    @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<ExperimentLogDTO> p = experimentService.listLogsWithDetails(userId, labId, status, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }
    @PutMapping("/logs/{id}")
    @PreAuthorize("hasAuthority('experiment:log')")
    public Result<ExperimentLog> updateLog(@PathVariable Long id, @RequestBody ExperimentLog log) {
        checkNotStudent();
        return Result.success(experimentService.updateLog(id, log));
    }
    @DeleteMapping("/logs/{id}")
    @PreAuthorize("hasAuthority('experiment:log')")
    public Result<Void> deleteLog(@PathVariable Long id) {
        checkNotStudent();
        experimentService.deleteLog(id);
        return Result.success(null);
    }
    @PostMapping("/logs/{id}/check-in")
    @PreAuthorize("hasAuthority('experiment:log')")
    public Result<ExperimentLog> checkInLog(@PathVariable Long id) {
        return Result.success(experimentService.checkInLog(id));
    }
    @PostMapping("/logs/{id}/check-out")
    @PreAuthorize("hasAuthority('experiment:log')")
    public Result<ExperimentLog> checkOutLog(@PathVariable Long id) {
        ExperimentLog log = experimentService.findLogById(id);
        if (log == null) throw new BusinessException(404, "实验记录不存在");
        Long currentUserId = SecurityUtils.currentUserId();
        if (!currentUserId.equals(log.getUserId()) && !isAdmin()) {
            throw new BusinessException(403, "只有实验发起者本人可以签退");
        }
        return Result.success(experimentService.checkOut(id));
    }
    @PostMapping("/logs/{id}/cancel")
    @PreAuthorize("hasAuthority('experiment:log')")
    public Result<ExperimentLog> cancelLog(@PathVariable Long id) {
        checkNotStudent();
        return Result.success(experimentService.cancelLog(id));
    }

    // ========== Experiment Record ==========
    @GetMapping("/records")
    @PreAuthorize("hasAuthority('experiment:log')")
    public Result<List<ExperimentRecord>> listRecords(@RequestParam(required = false) Long logId, @RequestParam(required = false) Long userId,
                                                       @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<ExperimentRecord> p = experimentService.listRecords(logId, userId, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }
    @PostMapping("/records")
    @PreAuthorize("hasAuthority('experiment:log')")
    public Result<ExperimentRecord> createRecord(@RequestBody ExperimentRecord r) { return Result.success(experimentService.createRecord(r)); }

    // ========== Material Requisition ==========
    @GetMapping("/requisitions")
    @PreAuthorize("hasAuthority('experiment:material')")
    public Result<List<MaterialRequisitionDTO>> listRequisitions(@RequestParam(required = false) Long userId, @RequestParam(required = false) String status,
                                                                  @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<MaterialRequisitionDTO> p = experimentService.listRequisitionsWithDetails(userId, status, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }
    @GetMapping("/requisitions/{id}")
    @PreAuthorize("hasAuthority('experiment:material')")
    public Result<MaterialRequisitionDTO> getRequisitionDetail(@PathVariable Long id) {
        return Result.success(experimentService.getRequisitionDetail(id));
    }
    @PostMapping("/requisitions")
    @PreAuthorize("hasAuthority('experiment:material')")
    public Result<MaterialRequisition> createRequisition(@RequestBody Map<String, Object> body) {
        MaterialRequisition r = new MaterialRequisition();
        r.setUserId(SecurityUtils.currentUserId());
        r.setExperimentLogId(body.get("experimentLogId") != null ? ((Number) body.get("experimentLogId")).longValue() : null);
        r.setExperimentProject((String) body.get("experimentProject"));
        return Result.success(experimentService.createRequisition(r, null));
    }
    @GetMapping("/requisitions/{id}/items")
    @PreAuthorize("hasAuthority('experiment:material')")
    public Result<List<MaterialRequisitionItem>> getRequisitionItems(@PathVariable Long id) { return Result.success(experimentService.getRequisitionItems(id)); }

    @PutMapping("/requisitions/{id}/approve")
    @PreAuthorize("hasAuthority('experiment:approve')")
    public Result<MaterialRequisition> approveRequisition(@PathVariable Long id) {
        return Result.success(experimentService.approveRequisition(id, SecurityUtils.currentUserId()));
    }
    @PutMapping("/requisitions/{id}/reject")
    @PreAuthorize("hasAuthority('experiment:approve')")
    public Result<MaterialRequisition> rejectRequisition(@PathVariable Long id) {
        return Result.success(experimentService.rejectRequisition(id));
    }
    @PutMapping("/requisitions/{id}/collect")
    @PreAuthorize("hasAuthority('experiment:approve')")
    public Result<MaterialRequisition> collectRequisition(@PathVariable Long id) {
        return Result.success(experimentService.collectRequisition(id));
    }

    // ========== Temporary Requisition ==========
    @GetMapping("/temp-requisitions")
    @PreAuthorize("hasAuthority('experiment:material')")
    public Result<List<TemporaryRequisition>> listTempRequisitions(@RequestParam(required = false) Long userId, @RequestParam(required = false) String status,
                                                                    @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<TemporaryRequisition> p = experimentService.listTempRequisitions(userId, status, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }
    @PostMapping("/temp-requisitions")
    @PreAuthorize("hasAuthority('experiment:material')")
    public Result<TemporaryRequisition> createTempRequisition(@RequestBody TemporaryRequisition r) { r.setUserId(SecurityUtils.currentUserId()); return Result.success(experimentService.createTempRequisition(r)); }

    // ========== Material Return ==========
    @GetMapping("/returns")
    @PreAuthorize("hasAuthority('experiment:material')")
    public Result<List<MaterialReturn>> listReturns(@RequestParam(required = false) Long userId, @RequestParam(required = false) String status,
                                                     @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<MaterialReturn> p = experimentService.listReturns(userId, status, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }
    @PostMapping("/returns")
    @PreAuthorize("hasAuthority('experiment:material')")
    public Result<MaterialReturn> createReturn(@RequestBody MaterialReturn r) { r.setUserId(SecurityUtils.currentUserId()); return Result.success(experimentService.createReturn(r)); }

    // ========== Inventory Check ==========
    @GetMapping("/inventory-checks")
    @PreAuthorize("hasAuthority('experiment:inventory')")
    public Result<List<InventoryCheckDTO>> listChecks(@RequestParam(required = false) Long consumableId, @RequestParam(required = false) String status,
                                                       @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<InventoryCheckDTO> p = experimentService.listChecksWithDetails(consumableId, status, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }
    @PostMapping("/inventory-checks")
    @PreAuthorize("hasAuthority('experiment:inventory')")
    public Result<InventoryCheck> createCheck(@RequestBody InventoryCheck c) { return Result.success(experimentService.createCheck(c)); }
    @PutMapping("/inventory-checks/{id}/complete")
    @PreAuthorize("hasAuthority('experiment:inventory')")
    public Result<InventoryCheck> completeCheck(@PathVariable Long id) { return Result.success(experimentService.completeCheck(id)); }
    @GetMapping("/inventory-checks/{id}/items")
    @PreAuthorize("hasAuthority('experiment:inventory')")
    public Result<List<InventoryCheckDTO>> getCheckItems(@PathVariable Long id) { return Result.success(experimentService.getCheckItems(id)); }
}
