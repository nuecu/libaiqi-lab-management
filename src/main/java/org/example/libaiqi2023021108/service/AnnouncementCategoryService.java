package org.example.libaiqi2023021108.service;

import org.example.libaiqi2023021108.common.BusinessException;
import org.example.libaiqi2023021108.entity.AnnouncementCategory;
import org.example.libaiqi2023021108.repository.AnnouncementCategoryRepository;
import org.example.libaiqi2023021108.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * 模块：公告管理
 * 功能：公告分类字典服务（真实持久化 CRUD）
 */

@Service
public class AnnouncementCategoryService {

    private final AnnouncementCategoryRepository categoryRepository;
    private final AnnouncementRepository announcementRepository;

    public AnnouncementCategoryService(AnnouncementCategoryRepository categoryRepository,
                                       AnnouncementRepository announcementRepository) {
        this.categoryRepository = categoryRepository;
        this.announcementRepository = announcementRepository;
    }

    /** 查询全部分类，并为每个分类填充其下公告的真实数量 */
    public List<AnnouncementCategory> list() {
        List<AnnouncementCategory> list = categoryRepository.findAllByOrderByIdAsc();
        list.forEach(c -> c.setCount(announcementRepository.countByAnnouncementType(c.getCategoryCode())));
        return list;
    }

    /** 仅查询启用状态的分类（供公告发布表单的下拉选项使用） */
    public List<AnnouncementCategory> listActive() {
        return categoryRepository.findAllByOrderByIdAsc().stream()
                .filter(c -> "ACTIVE".equals(c.getStatus())).toList();
    }

    @Transactional
    public AnnouncementCategory create(AnnouncementCategory c) {
        if (c.getCategoryName() == null || c.getCategoryName().isBlank()) throw new BusinessException("分类名称不能为空");
        if (c.getCategoryCode() == null || c.getCategoryCode().isBlank()) throw new BusinessException("分类编码不能为空");
        if (categoryRepository.existsByCategoryCode(c.getCategoryCode())) throw new BusinessException("分类编码已存在");
        c.setId(null);
        return categoryRepository.save(c);
    }

    @Transactional
    public AnnouncementCategory update(Long id, AnnouncementCategory c) {
        AnnouncementCategory ex = categoryRepository.findById(id).orElseThrow(() -> new BusinessException("分类不存在"));
        if (c.getCategoryName() != null && !c.getCategoryName().isBlank()) ex.setCategoryName(c.getCategoryName());
        // 分类编码作为公告的类型外键，创建后不可修改
        if (c.getDescription() != null) ex.setDescription(c.getDescription());
        if (c.getStatus() != null && !c.getStatus().isBlank()) ex.setStatus(c.getStatus());
        return categoryRepository.save(ex);
    }

    /** 删除分类：若仍有公告引用该分类编码则拒绝，防止产生孤立数据 */
    @Transactional
    public void delete(Long id) {
        AnnouncementCategory ex = categoryRepository.findById(id).orElseThrow(() -> new BusinessException("分类不存在"));
        long used = announcementRepository.countByAnnouncementType(ex.getCategoryCode());
        if (used > 0) throw new BusinessException("该分类下仍有 " + used + " 条公告，无法删除");
        categoryRepository.deleteById(id);
    }
}
