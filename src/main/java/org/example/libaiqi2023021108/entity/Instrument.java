package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * 模块：基础资源管理
 * 功能：仪器实体类，包含仪器名称、编码、品牌型号、所属实验室、状态等信息
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "instrument")
public class Instrument extends BaseEntity {
    @Column(name = "instrument_code", unique = true, nullable = false, length = 50)
    private String instrumentCode;

    @Column(name = "instrument_name", nullable = false, length = 200)
    private String instrumentName;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "specification", length = 500)
    private String specification;

    @Column(name = "purchase_price", precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "supplier", length = 200)
    private String supplier;

    @Column(name = "laboratory_id")
    private Long laboratoryId;

    @Column(name = "workstation_id")
    private Long workstationId;

    @Column(name = "status")
    private String status = "IDLE"; // IDLE, IN_USE, FAULT, MAINTENANCE, SCRAPPED

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "warranty_period_months")
    private Integer warrantyPeriodMonths;
}
