package org.example.libaiqi2023021108.controller;

import org.example.libaiqi2023021108.common.Result;
import org.example.libaiqi2023021108.dto.LaboratoryDTO;
import org.example.libaiqi2023021108.entity.*;
import org.example.libaiqi2023021108.service.LaboratoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * 模块：基础资源管理
 * 功能：实验室管理
 * 权限：读=任意登录用户(ROLE_AUTHENTICATED)；写(楼栋/实验室/工位增删改)=resource:laboratory:manage
 */

@RestController
@RequestMapping("/api/laboratories")
@PreAuthorize("hasAuthority('ROLE_AUTHENTICATED')")
public class LaboratoryController {
    private final LaboratoryService laboratoryService;
    public LaboratoryController(LaboratoryService laboratoryService) { this.laboratoryService = laboratoryService; }

    @GetMapping("/buildings")
    public Result<List<Building>> listBuildings(@RequestParam(required = false) String name,
                                                 @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<Building> p = laboratoryService.listBuildings(name, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }
    @GetMapping("/buildings/all")
    public Result<List<Building>> listAllBuildings() { return Result.success(laboratoryService.listAllBuildings()); }
    @PostMapping("/buildings")
    @PreAuthorize("hasAuthority('resource:laboratory:manage')")
    public Result<Building> createBuilding(@RequestBody Building b) { return Result.success(laboratoryService.createBuilding(b)); }
    @PutMapping("/buildings/{id}")
    @PreAuthorize("hasAuthority('resource:laboratory:manage')")
    public Result<Building> updateBuilding(@PathVariable Long id, @RequestBody Building b) { return Result.success(laboratoryService.updateBuilding(id, b)); }
    @DeleteMapping("/buildings/{id}")
    @PreAuthorize("hasAuthority('resource:laboratory:manage')")
    public Result<Void> deleteBuilding(@PathVariable Long id) { laboratoryService.deleteBuilding(id); return Result.success(); }

    @GetMapping
    public Result<List<LaboratoryDTO>> listLabs(@RequestParam(required = false) String name,
                                              @RequestParam(required = false) Long buildingId,
                                              @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<LaboratoryDTO> p = laboratoryService.listLabsWithDetails(name, buildingId, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }
    @GetMapping("/all")
    public Result<List<Laboratory>> listAllLabs() { return Result.success(laboratoryService.listAllLabs()); }
    @GetMapping("/{id}")
    public Result<Laboratory> getLabById(@PathVariable Long id) { return Result.success(laboratoryService.getLabById(id)); }
    @PostMapping
    @PreAuthorize("hasAuthority('resource:laboratory:manage')")
    public Result<Laboratory> createLab(@RequestBody Laboratory l) { return Result.success(laboratoryService.createLab(l)); }
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('resource:laboratory:manage')")
    public Result<Laboratory> updateLab(@PathVariable Long id, @RequestBody Laboratory l) { return Result.success(laboratoryService.updateLab(id, l)); }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('resource:laboratory:manage')")
    public Result<Void> deleteLab(@PathVariable Long id) { laboratoryService.deleteLab(id); return Result.success(); }

    @GetMapping("/workstations")
    public Result<List<Workstation>> listWorkstations(@RequestParam(required = false) String name,
                                                       @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<Workstation> p = laboratoryService.listWorkstations(name, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }
    @GetMapping("/workstations/lab/{labId}")
    public Result<List<Workstation>> listByLab(@PathVariable Long labId) { return Result.success(laboratoryService.listWorkstations(labId)); }
    @PostMapping("/workstations")
    @PreAuthorize("hasAuthority('resource:laboratory:manage')")
    public Result<Workstation> createWorkstation(@RequestBody Workstation w) { return Result.success(laboratoryService.createWorkstation(w)); }
    @PutMapping("/workstations/{id}")
    @PreAuthorize("hasAuthority('resource:laboratory:manage')")
    public Result<Workstation> updateWorkstation(@PathVariable Long id, @RequestBody Workstation w) { return Result.success(laboratoryService.updateWorkstation(id, w)); }
    @DeleteMapping("/workstations/{id}")
    @PreAuthorize("hasAuthority('resource:laboratory:manage')")
    public Result<Void> deleteWorkstation(@PathVariable Long id) { laboratoryService.deleteWorkstation(id); return Result.success(); }
}
