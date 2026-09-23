package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
/**
 * 模块：公告管理
 * 功能：公告分类字典实体，公告的 announcement_type 取值为分类编码（categoryCode）
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "announcement_category")
public class AnnouncementCategory extends BaseEntity {
    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;

    @Column(name = "category_code", nullable = false, unique = true, length = 50)
    private String categoryCode;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "status", length = 20)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE

    /** 该分类下公告数量，非数据库字段，由服务层统计填充 */
    @Transient
    private Long count;
}
