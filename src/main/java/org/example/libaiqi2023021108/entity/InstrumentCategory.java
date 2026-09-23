package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
/**
 * 模块：基础资源管理
 * 功能：仪器分类实体类，支持多级分类结构，用于仪器归类管理
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "instrument_category")
public class InstrumentCategory extends BaseEntity {
    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "status")
    private Integer status = 1;
}
