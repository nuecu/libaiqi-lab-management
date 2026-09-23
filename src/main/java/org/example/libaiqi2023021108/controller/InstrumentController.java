package org.example.libaiqi2023021108.controller;

import org.example.libaiqi2023021108.common.Result;
import org.example.libaiqi2023021108.entity.Instrument;
import org.example.libaiqi2023021108.entity.InstrumentCategory;
import org.example.libaiqi2023021108.service.InstrumentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * 模块：基础资源管理
 * 功能：仪器管理
 * 权限：读=任意登录用户(ROLE_AUTHENTICATED)；写(增删改/改状态)=resource:instrument:manage
 */

@RestController
@RequestMapping("/api/instruments")
@PreAuthorize("hasAuthority('ROLE_AUTHENTICATED')")
public class InstrumentController {
    private final InstrumentService instrumentService;
    public InstrumentController(InstrumentService instrumentService) { this.instrumentService = instrumentService; }

    @GetMapping("/categories")
    public Result<List<InstrumentCategory>> listCategories() { return Result.success(instrumentService.listCategories()); }

    @PostMapping("/categories")
    @PreAuthorize("hasAuthority('resource:instrument:manage')")
    public Result<InstrumentCategory> createCategory(@RequestBody InstrumentCategory c) { return Result.success(instrumentService.createCategory(c)); }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('resource:instrument:manage')")
    public Result<InstrumentCategory> updateCategory(@PathVariable Long id, @RequestBody InstrumentCategory c) { return Result.success(instrumentService.updateCategory(id, c)); }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('resource:instrument:manage')")
    public Result<Void> deleteCategory(@PathVariable Long id) { instrumentService.deleteCategory(id); return Result.success(); }

    @GetMapping
    public Result<List<Instrument>> list(@RequestParam(required = false) String name,
                                          @RequestParam(required = false) Long categoryId,
                                          @RequestParam(required = false) Long labId,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<Instrument> p = instrumentService.list(name, categoryId, labId, status, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }

    @GetMapping("/{id}")
    public Result<Instrument> getById(@PathVariable Long id) { return Result.success(instrumentService.getById(id)); }

    @PostMapping
    @PreAuthorize("hasAuthority('resource:instrument:manage')")
    public Result<Instrument> create(@RequestBody Instrument i) { return Result.success(instrumentService.create(i)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('resource:instrument:manage')")
    public Result<Instrument> update(@PathVariable Long id, @RequestBody Instrument i) { return Result.success(instrumentService.update(id, i)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('resource:instrument:manage')")
    public Result<Void> delete(@PathVariable Long id) { instrumentService.delete(id); return Result.success(); }

    @PutMapping("/{id}/status/{status}")
    @PreAuthorize("hasAuthority('resource:instrument:manage')")
    public Result<Void> updateStatus(@PathVariable Long id, @PathVariable String status) { instrumentService.updateStatus(id, status); return Result.success(); }
}
