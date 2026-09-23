package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
/**
 * 模块：基础资源管理
 * 功能：耗材分类实体类，支持多级分类结构，标识是否危化品
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "consumable_category")
public class ConsumableCategory extends BaseEntity {
    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "category_level")
    private Integer categoryLevel = 1;

    @Column(name = "is_hazardous")
    private Boolean isHazardous = false;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "status")
    private Integer status = 1;
}
