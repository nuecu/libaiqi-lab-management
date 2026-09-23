package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
import java.time.LocalDateTime;
/**
 * 模块：公告管理
 * 功能：公告实体类，包含公告标题、内容、类型、发布状态、是否置顶等
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "announcement")
public class Announcement extends BaseEntity {
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "announcement_type", length = 20)
    private String announcementType; // NOTICE, NEWS, POLICY

    @Column(name = "status")
    private Integer status = 1; // 1-发布 0-草稿

    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    @Column(name = "is_top")
    private Boolean isTop = false;

    /** 发布人展示名（真实姓名），非数据库字段，由服务层在返回前解析填充 */
    @Transient
    private String createByName;
}
