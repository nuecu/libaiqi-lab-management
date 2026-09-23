package org.example.libaiqi2023021108.controller;

import org.example.libaiqi2023021108.common.Result;
import org.example.libaiqi2023021108.entity.Consumable;
import org.example.libaiqi2023021108.entity.ConsumableCategory;
import org.example.libaiqi2023021108.service.ConsumableService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * 模块：基础资源管理
 * 功能：耗材管理
 * 权限：读=任意登录用户(ROLE_AUTHENTICATED)；写(增删改/改库存)=resource:consumable:manage
 */

@RestController
@RequestMapping("/api/consumables")
@PreAuthorize("hasAuthority('ROLE_AUTHENTICATED')")
public class ConsumableController {
    private final ConsumableService consumableService;
    public ConsumableController(ConsumableService consumableService) { this.consumableService = consumableService; }

    @GetMapping("/categories")
    public Result<List<ConsumableCategory>> listCategories() { return Result.success(consumableService.listCategories()); }

    @PostMapping("/categories")
    @PreAuthorize("hasAuthority('resource:consumable:manage')")
    public Result<ConsumableCategory> createCategory(@RequestBody ConsumableCategory c) { return Result.success(consumableService.createCategory(c)); }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('resource:consumable:manage')")
    public Result<ConsumableCategory> updateCategory(@PathVariable Long id, @RequestBody ConsumableCategory c) { return Result.success(consumableService.updateCategory(id, c)); }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('resource:consumable:manage')")
    public Result<Void> deleteCategory(@PathVariable Long id) { consumableService.deleteCategory(id); return Result.success(); }

    @GetMapping
    public Result<List<Consumable>> list(@RequestParam(required = false) String name,
                                          @RequestParam(required = false) Long categoryId,
                                          @RequestParam(required = false) Boolean hazardous,
                                          @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<Consumable> p = consumableService.list(name, categoryId, hazardous, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }

    @GetMapping("/{id}")
    public Result<Consumable> getById(@PathVariable Long id) { return Result.success(consumableService.getById(id)); }

    @PostMapping
    @PreAuthorize("hasAuthority('resource:consumable:manage')")
    public Result<Consumable> create(@RequestBody Consumable c) { return Result.success(consumableService.create(c)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('resource:consumable:manage')")
    public Result<Consumable> update(@PathVariable Long id, @RequestBody Consumable c) { return Result.success(consumableService.update(id, c)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('resource:consumable:manage')")
    public Result<Void> delete(@PathVariable Long id) { consumableService.delete(id); return Result.success(); }

    @GetMapping("/low-stock")
    public Result<List<Consumable>> lowStock() { return Result.success(consumableService.getLowStockItems()); }

    @PutMapping("/{id}/stock/{quantity}")
    @PreAuthorize("hasAuthority('resource:consumable:manage')")
    public Result<Void> adjustStock(@PathVariable Long id, @PathVariable int quantity) { consumableService.adjustStock(id, quantity); return Result.success(); }
}
