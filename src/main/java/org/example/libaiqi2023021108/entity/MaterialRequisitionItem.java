package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
/**
 * 模块：实验管理
 * 功能：领用单明细项实体类，记录领用的耗材ID、申请数量、实际发放数量
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "material_requisition_item")
public class MaterialRequisitionItem extends BaseEntity {
    @Column(name = "requisition_id", nullable = false)
    private Long requisitionId;

    @Column(name = "consumable_id", nullable = false)
    private Long consumableId;

    @Column(name = "consumable_name", length = 200)
    private String consumableName;

    @Column(name = "request_quantity", nullable = false)
    private Integer requestQuantity;

    @Column(name = "actual_quantity")
    private Integer actualQuantity;

    @Column(name = "unit", length = 20)
    private String unit;
}
