package org.example.libaiqi2023021108.controller;

import org.example.libaiqi2023021108.annotation.LogAction;
import org.example.libaiqi2023021108.common.Result;
import org.example.libaiqi2023021108.common.SecurityUtils;
import org.example.libaiqi2023021108.dto.ReservationDTO;
import org.example.libaiqi2023021108.entity.Reservation;
import org.example.libaiqi2023021108.entity.ReservationRule;
import org.example.libaiqi2023021108.service.ReservationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
/**
 * 模块：预约管理
 * 功能：预约管理
 * 权限：/rules 预约规则需要 reservation:rules，其余预约业务需要 reservation:list
 */

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;
    public ReservationController(ReservationService reservationService) { this.reservationService = reservationService; }

    @GetMapping("/rules")
    @PreAuthorize("hasAuthority('reservation:rules')")
    public Result<List<ReservationRule>> listRules(@RequestParam(required = false) String resourceType) { return Result.success(reservationService.listRules(resourceType)); }
    @PostMapping("/rules")
    @PreAuthorize("hasAuthority('reservation:rules')")
    public Result<ReservationRule> createRule(@RequestBody ReservationRule r) { return Result.success(reservationService.createRule(r)); }
    @PutMapping("/rules/{id}")
    @PreAuthorize("hasAuthority('reservation:rules')")
    public Result<ReservationRule> updateRule(@PathVariable Long id, @RequestBody ReservationRule r) { return Result.success(reservationService.updateRule(id, r)); }
    @DeleteMapping("/rules/{id}")
    @PreAuthorize("hasAuthority('reservation:rules')")
    public Result<Void> deleteRule(@PathVariable Long id) { reservationService.deleteRule(id); return Result.success(); }

    @GetMapping
    @PreAuthorize("hasAuthority('reservation:list')")
    public Result<List<ReservationDTO>> list(@RequestParam(required = false) Long userId,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<ReservationDTO> p = reservationService.listWithDetails(userId, status, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }
    @GetMapping("/resource")
    @PreAuthorize("hasAuthority('reservation:list')")
    public Result<List<Reservation>> listByResource(@RequestParam Long resourceId, @RequestParam String resourceType,
                                                     @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<Reservation> p = reservationService.listByResource(resourceId, resourceType, PageRequest.of(page, size));
        return Result.success(p.getContent(), p.getTotalElements());
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('reservation:list')")
    public Result<Reservation> getById(@PathVariable Long id) { return Result.success(reservationService.getById(id)); }
    @PostMapping
    @PreAuthorize("hasAuthority('reservation:list')")
    @LogAction("提交预约")
    public Result<Reservation> create(@RequestBody Reservation r) { r.setUserId(SecurityUtils.currentUserId()); return Result.success(reservationService.create(r)); }
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('reservation:approve')")
    @LogAction("审批通过预约")
    public Result<Reservation> approve(@PathVariable Long id) { return Result.success(reservationService.approve(id, SecurityUtils.currentUserId())); }
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('reservation:approve')")
    @LogAction("驳回预约")
    public Result<Reservation> reject(@PathVariable Long id, @RequestBody Map<String, Object> body) { return Result.success(reservationService.reject(id, SecurityUtils.currentUserId(), (String) body.get("reason"))); }
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('reservation:list')")
    @LogAction("取消预约")
    public Result<Reservation> cancel(@PathVariable Long id) { return Result.success(reservationService.cancel(id, SecurityUtils.currentUserId())); }
    @PostMapping("/{id}/checkin")
    @PreAuthorize("hasAuthority('reservation:list')")
    @LogAction("预约签到")
    public Result<Reservation> checkIn(@PathVariable Long id) { return Result.success(reservationService.checkIn(id, SecurityUtils.currentUserId())); }
    @PostMapping("/{id}/checkout")
    @PreAuthorize("hasAuthority('reservation:list')")
    @LogAction("预约签退")
    public Result<Reservation> checkOut(@PathVariable Long id) { return Result.success(reservationService.checkOut(id, SecurityUtils.currentUserId())); }
}
