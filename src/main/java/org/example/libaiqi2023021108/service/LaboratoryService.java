package org.example.libaiqi2023021108.service;

import org.example.libaiqi2023021108.common.BusinessException;
import org.example.libaiqi2023021108.dto.LaboratoryDTO;
import org.example.libaiqi2023021108.entity.Building;
import org.example.libaiqi2023021108.entity.Laboratory;
import org.example.libaiqi2023021108.entity.Workstation;
import org.example.libaiqi2023021108.repository.BuildingRepository;
import org.example.libaiqi2023021108.repository.LaboratoryRepository;
import org.example.libaiqi2023021108.repository.WorkstationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
/**
 * 模块：基础资源管理
 * 功能：实验室管理服务
 */

@Service
public class LaboratoryService {

    private final LaboratoryRepository labRepository;
    private final BuildingRepository buildingRepository;
    private final WorkstationRepository workstationRepository;

    public LaboratoryService(LaboratoryRepository labRepository, BuildingRepository buildingRepository,
                             WorkstationRepository workstationRepository) {
        this.labRepository = labRepository;
        this.buildingRepository = buildingRepository;
        this.workstationRepository = workstationRepository;
    }

    // Building
    public Page<Building> listBuildings(String name, Pageable pageable) {
        if (name != null && !name.isEmpty()) return buildingRepository.findByBuildingNameContaining(name, pageable);
        return buildingRepository.findAll(pageable);
    }
    public List<Building> listAllBuildings() { return buildingRepository.findAll(); }
    public Building createBuilding(Building b) { return buildingRepository.save(b); }
    public Building updateBuilding(Long id, Building b) {
        Building ex = buildingRepository.findById(id).orElseThrow(() -> new BusinessException("楼宇不存在"));
        ex.setBuildingName(b.getBuildingName()); ex.setBuildingCode(b.getBuildingCode());
        ex.setFloors(b.getFloors()); ex.setAddress(b.getAddress()); ex.setStatus(b.getStatus());
        return buildingRepository.save(ex);
    }
    public void deleteBuilding(Long id) { buildingRepository.deleteById(id); }

    // Laboratory
    public Page<Laboratory> listLabs(String name, Long buildingId, Pageable pageable) {
        if (name != null && !name.isEmpty()) return labRepository.findByLabNameContaining(name, pageable);
        if (buildingId != null) return labRepository.findByBuildingId(buildingId, pageable);
        return labRepository.findAll(pageable);
    }

    /**
     * 返回包含楼栋名称的DTO列表
     */
    public Page<LaboratoryDTO> listLabsWithDetails(String name, Long buildingId, Pageable pageable) {
        Page<Laboratory> page = listLabs(name, buildingId, pageable);
        return page.map(this::convertToDTO);
    }

    private LaboratoryDTO convertToDTO(Laboratory l) {
        LaboratoryDTO dto = new LaboratoryDTO();
        dto.setId(l.getId());
        dto.setLabName(l.getLabName());
        dto.setLabCode(l.getLabCode());
        dto.setBuildingId(l.getBuildingId());
        dto.setFloorNumber(l.getFloorNumber());
        dto.setRoomNumber(l.getRoomNumber());
        dto.setCapacity(l.getCapacity());
        dto.setAreaSqm(l.getAreaSqm());
        dto.setLabType(l.getLabType());
        dto.setManagerId(l.getManagerId());
        dto.setOpenStartTime(l.getOpenStartTime());
        dto.setOpenEndTime(l.getOpenEndTime());
        dto.setStatus(l.getStatus());
        // 获取楼栋名称
        if (l.getBuildingId() != null) {
            buildingRepository.findById(l.getBuildingId()).ifPresent(b -> dto.setBuildingName(b.getBuildingName()));
        }
        return dto;
    }
    public List<Laboratory> listAllLabs() { return labRepository.findByStatus(1); }
    public Laboratory getLabById(Long id) { return labRepository.findById(id).orElseThrow(() -> new BusinessException("实验室不存在")); }
    public Laboratory createLab(Laboratory l) { return labRepository.save(l); }
    public Laboratory updateLab(Long id, Laboratory l) {
        Laboratory ex = getLabById(id);
        ex.setLabName(l.getLabName()); ex.setLabCode(l.getLabCode()); ex.setBuildingId(l.getBuildingId());
        ex.setFloorNumber(l.getFloorNumber()); ex.setRoomNumber(l.getRoomNumber());
        ex.setCapacity(l.getCapacity()); ex.setAreaSqm(l.getAreaSqm()); ex.setLabType(l.getLabType());
        ex.setManagerId(l.getManagerId()); ex.setOpenStartTime(l.getOpenStartTime());
        ex.setOpenEndTime(l.getOpenEndTime()); ex.setStatus(l.getStatus());
        return labRepository.save(ex);
    }
    public void deleteLab(Long id) { labRepository.deleteById(id); }

    // Workstation
    public List<Workstation> listWorkstations(Long labId) { return workstationRepository.findByLaboratoryId(labId); }
    public Page<Workstation> listWorkstations(String name, Pageable pageable) {
        if (name != null && !name.isEmpty()) return workstationRepository.findByWorkstationNameContaining(name, pageable);
        return workstationRepository.findAll(pageable);
    }
    public Workstation createWorkstation(Workstation w) { return workstationRepository.save(w); }
    public Workstation updateWorkstation(Long id, Workstation w) {
        Workstation ex = workstationRepository.findById(id).orElseThrow(() -> new BusinessException("工位不存在"));
        ex.setWorkstationName(w.getWorkstationName()); ex.setWorkstationCode(w.getWorkstationCode());
        ex.setLaboratoryId(w.getLaboratoryId()); ex.setPositionDesc(w.getPositionDesc());
        ex.setCapacity(w.getCapacity()); ex.setStatus(w.getStatus());
        return workstationRepository.save(ex);
    }
    public void deleteWorkstation(Long id) { workstationRepository.deleteById(id); }
}
