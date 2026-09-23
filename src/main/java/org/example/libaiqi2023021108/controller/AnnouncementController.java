package org.example.libaiqi2023021108.controller;

import org.example.libaiqi2023021108.annotation.LogAction;
import org.example.libaiqi2023021108.common.Result;
import org.example.libaiqi2023021108.entity.Announcement;
import org.example.libaiqi2023021108.service.AnnouncementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * 模块：公告管理
 * 功能：公告管理
 * 权限：读(公告列表/详情)=任意登录用户(ROLE_AUTHENTICATED)；写(发布/改/删)=announcement:publish
 */

@RestController
@RequestMapping("/api/announcements")
@PreAuthorize("hasAuthority('ROLE_AUTHENTICATED')")
public class AnnouncementController {
    private final AnnouncementService announcementService;
    public AnnouncementController(AnnouncementService announcementService) { this.announcementService = announcementService; }

    @GetMapping
    public Result<List<Announcement>> list(@RequestParam(required = false) String title, @RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<Announcement> p = announcementService.list(title, status, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }
    @GetMapping("/{id}")
    public Result<Announcement> getById(@PathVariable Long id) { return Result.success(announcementService.getById(id)); }
    @PostMapping
    @PreAuthorize("hasAuthority('announcement:publish')")
    @LogAction("新增公告")
    public Result<Announcement> create(@RequestBody Announcement a) { return Result.success(announcementService.create(a)); }
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('announcement:publish')")
    @LogAction("修改公告")
    public Result<Announcement> update(@PathVariable Long id, @RequestBody Announcement a) { return Result.success(announcementService.update(id, a)); }
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('announcement:publish')")
    @LogAction("发布公告")
    public Result<Announcement> publish(@PathVariable Long id) { return Result.success(announcementService.publish(id)); }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('announcement:publish')")
    @LogAction("删除公告")
    public Result<Void> delete(@PathVariable Long id) { announcementService.delete(id); return Result.success(); }
}
