package org.example.libaiqi2023021108.service;

import org.example.libaiqi2023021108.common.BusinessException;
import org.example.libaiqi2023021108.entity.Instrument;
import org.example.libaiqi2023021108.entity.InstrumentCategory;
import org.example.libaiqi2023021108.repository.InstrumentRepository;
import org.example.libaiqi2023021108.repository.InstrumentCategoryRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * 模块：基础资源管理
 * 功能：仪器管理服务
 */

@Service
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;
    private final InstrumentCategoryRepository categoryRepository;

    public InstrumentService(InstrumentRepository instrumentRepository, InstrumentCategoryRepository categoryRepository) {
        this.instrumentRepository = instrumentRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<InstrumentCategory> listCategories() { return categoryRepository.findAll(); }
    public InstrumentCategory createCategory(InstrumentCategory c) { return categoryRepository.save(c); }
    public InstrumentCategory updateCategory(Long id, InstrumentCategory c) {
        InstrumentCategory ex = categoryRepository.findById(id).orElseThrow(() -> new BusinessException("分类不存在"));
        ex.setCategoryName(c.getCategoryName()); ex.setParentId(c.getParentId());
        ex.setSortOrder(c.getSortOrder()); ex.setStatus(c.getStatus());
        return categoryRepository.save(ex);
    }
    public void deleteCategory(Long id) { categoryRepository.deleteById(id); }

    /** 分页查询仪器列表（使用Redis缓存），支持按名称、分类、实验室、状态筛选 */
    @Cacheable(value = "instruments", key = "#name + '|' + #categoryId + '|' + #labId + '|' + #status + '|' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Instrument> list(String name, Long categoryId, Long labId, String status, Pageable pageable) {
        if (name != null && !name.isEmpty()) return instrumentRepository.findByInstrumentNameContaining(name, pageable);
        if (categoryId != null) return instrumentRepository.findByCategoryId(categoryId, pageable);
        if (labId != null) return instrumentRepository.findByLaboratoryId(labId, pageable);
        if (status != null && !status.isEmpty()) return instrumentRepository.findByStatus(status, pageable);
        return instrumentRepository.findAll(pageable);
    }

    public Instrument getById(Long id) { return instrumentRepository.findById(id).orElseThrow(() -> new BusinessException("仪器不存在")); }

    @CacheEvict(value = "instruments", allEntries = true)
    public Instrument create(Instrument i) {
        if (instrumentRepository.existsByInstrumentCode(i.getInstrumentCode())) throw new BusinessException("仪器编码已存在");
        return instrumentRepository.save(i);
    }

    @CacheEvict(value = "instruments", allEntries = true)
    public Instrument update(Long id, Instrument i) {
        Instrument ex = getById(id);
        ex.setInstrumentName(i.getInstrumentName()); ex.setCategoryId(i.getCategoryId());
        ex.setBrand(i.getBrand()); ex.setModel(i.getModel()); ex.setSpecification(i.getSpecification());
        ex.setPurchasePrice(i.getPurchasePrice()); ex.setPurchaseDate(i.getPurchaseDate());
        ex.setSupplier(i.getSupplier()); ex.setLaboratoryId(i.getLaboratoryId());
        ex.setWorkstationId(i.getWorkstationId()); ex.setStatus(i.getStatus());
        ex.setImageUrl(i.getImageUrl()); ex.setWarrantyPeriodMonths(i.getWarrantyPeriodMonths());
        return instrumentRepository.save(ex);
    }

    @CacheEvict(value = "instruments", allEntries = true)
    public void delete(Long id) { instrumentRepository.deleteById(id); }

    @CacheEvict(value = "instruments", allEntries = true)
    public void updateStatus(Long id, String status) {
        Instrument i = getById(id);
        i.setStatus(status);
        instrumentRepository.save(i);
    }
}
