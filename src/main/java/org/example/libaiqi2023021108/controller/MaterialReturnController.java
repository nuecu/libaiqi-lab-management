package org.example.libaiqi2023021108.controller;

import org.example.libaiqi2023021108.common.Result;
import org.example.libaiqi2023021108.common.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.example.libaiqi2023021108.dto.MaterialReturnDTO;
import org.example.libaiqi2023021108.entity.MaterialReturn;
import org.example.libaiqi2023021108.service.MaterialReturnService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * 模块：设备运维
 * 功能：耗材归还
 */

@RestController
@RequestMapping("/api/material-returns")
public class MaterialReturnController {
    private final MaterialReturnService returnService;
    public MaterialReturnController(MaterialReturnService returnService) { this.returnService = returnService; }

    @GetMapping
    @PreAuthorize("hasAuthority('equipment:return')")
    public Result<List<MaterialReturnDTO>> list(@RequestParam(required = false) Long userId,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        Page<MaterialReturnDTO> p = returnService.listWithDetails(userId, status, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('equipment:return')")
    public Result<MaterialReturn> getById(@PathVariable Long id) { return Result.success(returnService.getById(id)); }

    @PostMapping
    @PreAuthorize("hasAuthority('equipment:return')")
    public Result<MaterialReturn> create(@RequestBody MaterialReturn r) { return Result.success(returnService.create(r)); }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('equipment:return')")
    public Result<MaterialReturn> complete(@PathVariable Long id) {
        return Result.success(returnService.complete(id, SecurityUtils.currentUserId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('equipment:return')")
    public Result<Void> delete(@PathVariable Long id) { returnService.delete(id); return Result.success(); }
}
