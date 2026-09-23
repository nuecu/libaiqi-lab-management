package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
import java.math.BigDecimal;
/**
 * 模块：基础资源管理
 * 功能：耗材实体类，包含耗材名称、编码、规格、库存量、危化品标识、存储位置等信息
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "consumable")
public class Consumable extends BaseEntity {
    @Column(name = "consumable_code", unique = true, nullable = false, length = 50)
    private String consumableCode;

    @Column(name = "consumable_name", nullable = false, length = 200)
    private String consumableName;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "specification", length = 200)
    private String specification;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "supplier", length = 200)
    private String supplier;

    @Column(name = "purchase_price", precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "current_stock")
    private Integer currentStock = 0;

    @Column(name = "min_stock")
    private Integer minStock = 10;

    @Column(name = "max_stock")
    private Integer maxStock = 1000;

    @Column(name = "is_hazardous")
    private Boolean isHazardous = false;

    @Column(name = "storage_location", length = 200)
    private String storageLocation;

    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;

    @Column(name = "status")
    private Integer status = 1;

    @Column(name = "image_url", length = 500)
    private String imageUrl;
}
