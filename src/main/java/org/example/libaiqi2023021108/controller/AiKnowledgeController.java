package org.example.libaiqi2023021108.controller;

import org.example.libaiqi2023021108.annotation.LogAction;
import org.example.libaiqi2023021108.common.Result;
import org.example.libaiqi2023021108.entity.AiKnowledge;
import org.example.libaiqi2023021108.service.AiKnowledgeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * 模块：AI助手
 * 功能：受限知识库管理——条目定义AI可回答的范围，仅管理员/授权者可维护
 * 权限：读=任意登录用户；写(新增/改/删)=ai:knowledge:manage
 */
@RestController
@RequestMapping("/api/ai/knowledge")
@PreAuthorize("hasAuthority('ROLE_AUTHENTICATED')")
public class AiKnowledgeController {

    private final AiKnowledgeService service;
    public AiKnowledgeController(AiKnowledgeService service) { this.service = service; }

    @GetMapping
    public Result<List<AiKnowledge>> list(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String category,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        Page<AiKnowledge> p = service.list(keyword, category,
                PageRequest.of(page, size, Sort.by("id").ascending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }

    @GetMapping("/{id}")
    public Result<AiKnowledge> getById(@PathVariable Long id) { return Result.success(service.getById(id)); }

    @PostMapping
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @LogAction("新增AI知识条目")
    public Result<AiKnowledge> create(@RequestBody AiKnowledge k) { return Result.success(service.create(k)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @LogAction("修改AI知识条目")
    public Result<AiKnowledge> update(@PathVariable Long id, @RequestBody AiKnowledge k) { return Result.success(service.update(id, k)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ai:knowledge:manage')")
    @LogAction("删除AI知识条目")
    public Result<Void> delete(@PathVariable Long id) { service.delete(id); return Result.success(); }
}
