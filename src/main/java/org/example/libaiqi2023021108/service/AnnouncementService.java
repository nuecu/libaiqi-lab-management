package org.example.libaiqi2023021108.service;

import org.example.libaiqi2023021108.common.SecurityUtils;
import org.example.libaiqi2023021108.entity.Announcement;
import org.example.libaiqi2023021108.entity.SysUser;
import org.example.libaiqi2023021108.repository.AnnouncementRepository;
import org.example.libaiqi2023021108.repository.SysUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
/**
 * 模块：公告管理
 * 功能：公告管理服务
 */

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final SysUserRepository sysUserRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository, SysUserRepository sysUserRepository) {
        this.announcementRepository = announcementRepository;
        this.sysUserRepository = sysUserRepository;
    }

    /** 分页查询公告，支持按标题模糊搜索和状态筛选 */
    public Page<Announcement> list(String title, Integer status, Pageable pageable) {
        Page<Announcement> page;
        if (title != null && !title.isEmpty()) page = announcementRepository.findByTitleContaining(title, pageable);
        else if (status != null) page = announcementRepository.findByStatus(status, pageable);
        else page = announcementRepository.findAll(pageable);
        page.getContent().forEach(this::resolveCreateByName);
        return page;
    }

    public Announcement getById(Long id) {
        Announcement a = announcementRepository.findById(id).orElse(null);
        resolveCreateByName(a);
        return a;
    }

    /** 新建公告：以发布者用户名作为稳定的 create_by，若为发布态则补全发布时间 */
    public Announcement create(Announcement a) {
        SysUser me = currentUser();
        if (me != null) a.setCreateBy(me.getUsername());
        if (a.getStatus() == null) a.setStatus(1);
        if (a.getStatus() != null && a.getStatus() == 1 && a.getPublishTime() == null) a.setPublishTime(LocalDateTime.now());
        Announcement saved = announcementRepository.save(a);
        resolveCreateByName(saved);
        return saved;
    }

    /** 更新公告（标题、内容、类型、状态、是否置顶等），保留原发布者 */
    public Announcement update(Long id, Announcement a) {
        Announcement ex = announcementRepository.findById(id).orElse(null);
        if (ex != null) {
            ex.setTitle(a.getTitle()); ex.setContent(a.getContent());
            ex.setAnnouncementType(a.getAnnouncementType()); ex.setStatus(a.getStatus());
            ex.setIsTop(a.getIsTop());
            if (a.getPublishTime() != null) ex.setPublishTime(a.getPublishTime());
            Announcement saved = announcementRepository.save(ex);
            resolveCreateByName(saved);
            return saved;
        }
        return null;
    }

    /** 发布公告：置为已发布(状态1)并写入发布时间与发布者用户名（若缺失） */
    public Announcement publish(Long id) {
        Announcement ex = announcementRepository.findById(id).orElse(null);
        if (ex == null) return null;
        ex.setStatus(1);
        if (ex.getPublishTime() == null) ex.setPublishTime(LocalDateTime.now());
        if (ex.getCreateBy() == null || ex.getCreateBy().isEmpty()) {
            SysUser me = currentUser();
            if (me != null) ex.setCreateBy(me.getUsername());
        }
        Announcement saved = announcementRepository.save(ex);
        resolveCreateByName(saved);
        return saved;
    }

    public void delete(Long id) { announcementRepository.deleteById(id); }

    /** 解析当前登录用户实体（找不到返回 null） */
    private SysUser currentUser() {
        try {
            Long uid = SecurityUtils.currentUserId();
            return sysUserRepository.findById(uid).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将公告的发布人标识（create_by，可能是用户名 / 数字用户ID / 已存的真实姓名）
     * 解析为展示用的真实姓名，填充到非持久化字段 createByName。
     */
    private void resolveCreateByName(Announcement a) {
        if (a == null) return;
        String stored = a.getCreateBy();
        if (stored == null || stored.trim().isEmpty()) { a.setCreateByName(null); return; }
        String key = stored.trim();
        // 1) 命中用户名 -> 用真实姓名
        SysUser byName = sysUserRepository.findByUsername(key).orElse(null);
        if (byName != null) {
            a.setCreateByName(displayOf(byName));
            return;
        }
        // 2) 命中数字用户ID -> 用真实姓名
        if (key.chars().allMatch(Character::isDigit)) {
            try {
                SysUser byId = sysUserRepository.findById(Long.valueOf(key)).orElse(null);
                if (byId != null) { a.setCreateByName(displayOf(byId)); return; }
            } catch (NumberFormatException ignore) { }
        }
        // 3) 已是展示名，直接沿用
        a.setCreateByName(key);
    }

    private String displayOf(SysUser u) {
        return (u.getRealName() != null && !u.getRealName().isEmpty()) ? u.getRealName() : u.getUsername();
    }
}
