package org.example.libaiqi2023021108.controller;

import org.example.libaiqi2023021108.common.Result;
import org.example.libaiqi2023021108.entity.AnnouncementCategory;
import org.example.libaiqi2023021108.service.AnnouncementCategoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * 模块：公告管理
 * 功能：公告分类字典管理
 * 权限：读=任意登录用户（发布表单需拉取分类选项）；写(新增/改/删)=announcement:category
 */

@RestController
@RequestMapping("/api/announcement-categories")
@PreAuthorize("hasAuthority('ROLE_AUTHENTICATED')")
public class AnnouncementCategoryController {
    private final AnnouncementCategoryService categoryService;
    public AnnouncementCategoryController(AnnouncementCategoryService categoryService) { this.categoryService = categoryService; }

    @GetMapping
    public Result<List<AnnouncementCategory>> list() { return Result.success(categoryService.list()); }

    @GetMapping("/active")
    public Result<List<AnnouncementCategory>> listActive() { return Result.success(categoryService.listActive()); }

    @PostMapping
    @PreAuthorize("hasAuthority('announcement:category')")
    public Result<AnnouncementCategory> create(@RequestBody AnnouncementCategory c) { return Result.success(categoryService.create(c)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('announcement:category')")
    public Result<AnnouncementCategory> update(@PathVariable Long id, @RequestBody AnnouncementCategory c) { return Result.success(categoryService.update(id, c)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('announcement:category')")
    public Result<Void> delete(@PathVariable Long id) { categoryService.delete(id); return Result.success(); }
}
