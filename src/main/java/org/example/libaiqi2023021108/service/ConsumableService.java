package org.example.libaiqi2023021108.service;

import org.example.libaiqi2023021108.common.BusinessException;
import org.example.libaiqi2023021108.entity.Consumable;
import org.example.libaiqi2023021108.entity.ConsumableCategory;
import org.example.libaiqi2023021108.repository.ConsumableRepository;
import org.example.libaiqi2023021108.repository.ConsumableCategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * 模块：基础资源管理
 * 功能：耗材管理服务
 */

@Service
public class ConsumableService {

    private final ConsumableRepository consumableRepository;
    private final ConsumableCategoryRepository categoryRepository;

    public ConsumableService(ConsumableRepository consumableRepository, ConsumableCategoryRepository categoryRepository) {
        this.consumableRepository = consumableRepository;
        this.categoryRepository = categoryRepository;
    }

    // Category
    public List<ConsumableCategory> listCategories() { return categoryRepository.findAll(); }
    /** 根据父分类ID查询子分类列表 */
    public List<ConsumableCategory> listCategoriesByParent(Long parentId) { return categoryRepository.findByParentId(parentId); }
    public ConsumableCategory createCategory(ConsumableCategory c) { return categoryRepository.save(c); }
    public ConsumableCategory updateCategory(Long id, ConsumableCategory c) {
        ConsumableCategory ex = categoryRepository.findById(id).orElseThrow(() -> new BusinessException("分类不存在"));
        ex.setCategoryName(c.getCategoryName()); ex.setParentId(c.getParentId());
        ex.setIsHazardous(c.getIsHazardous()); ex.setSortOrder(c.getSortOrder()); ex.setStatus(c.getStatus());
        return categoryRepository.save(ex);
    }
    public void deleteCategory(Long id) { categoryRepository.deleteById(id); }

    // Consumable
    public Page<Consumable> list(String name, Long categoryId, Boolean hazardous, Pageable pageable) {
        if (name != null && !name.isEmpty()) return consumableRepository.findByConsumableNameContaining(name, pageable);
        if (categoryId != null) return consumableRepository.findByCategoryId(categoryId, pageable);
        if (hazardous != null) return consumableRepository.findByIsHazardous(hazardous, pageable);
        return consumableRepository.findAll(pageable);
    }

    public Consumable getById(Long id) { return consumableRepository.findById(id).orElseThrow(() -> new BusinessException("耗材不存在")); }

    /** 新增耗材，编码不能重复 */
    public Consumable create(Consumable c) {
        if (consumableRepository.existsByConsumableCode(c.getConsumableCode())) throw new BusinessException("耗材编码已存在");
        return consumableRepository.save(c);
    }

    public Consumable update(Long id, Consumable c) {
        Consumable ex = getById(id);
        ex.setConsumableName(c.getConsumableName()); ex.setCategoryId(c.getCategoryId());
        ex.setSpecification(c.getSpecification()); ex.setUnit(c.getUnit()); ex.setBrand(c.getBrand());
        ex.setModel(c.getModel()); ex.setSupplier(c.getSupplier()); ex.setPurchasePrice(c.getPurchasePrice());
        ex.setMinStock(c.getMinStock()); ex.setMaxStock(c.getMaxStock());
        ex.setIsHazardous(c.getIsHazardous()); ex.setStorageLocation(c.getStorageLocation());
        ex.setShelfLifeDays(c.getShelfLifeDays()); ex.setStatus(c.getStatus()); ex.setImageUrl(c.getImageUrl());
        return consumableRepository.save(ex);
    }

    public void delete(Long id) { consumableRepository.deleteById(id); }

    /** 查询库存低于最低阈值的耗材列表 */
    public List<Consumable> getLowStockItems() { return consumableRepository.findLowStockItems(); }
    /** 查询库存超过最高阈值的耗材列表 */
    public List<Consumable> getOverStockItems() { return consumableRepository.findOverStockItems(); }

    /** 调整耗材库存（正数增加，负数减少） */
    public void adjustStock(Long id, int quantity) {
        Consumable c = getById(id);
        c.setCurrentStock(c.getCurrentStock() + quantity);
        consumableRepository.save(c);
    }
}
