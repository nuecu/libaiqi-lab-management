package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
import java.time.LocalDate;
/**
 * 模块：实验管理
 * 功能：盘点记录实体类，包含盘点编号、盘点人、系统数量、实际数量、盈亏差异等
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "inventory_check")
public class InventoryCheck extends BaseEntity {
    @Column(name = "check_no", unique = true, nullable = false, length = 50)
    private String checkNo;

    @Column(name = "check_name", length = 200)
    private String checkName;

    @Column(name = "check_date")
    private LocalDate checkDate;

    @Column(name = "checker_id")
    private Long checkerId;

    @Column(name = "consumable_id")
    private Long consumableId;

    @Column(name = "system_quantity")
    private Integer systemQuantity;

    @Column(name = "actual_quantity")
    private Integer actualQuantity;

    @Column(name = "difference")
    private Integer difference;

    @Column(name = "loss_reason", length = 500)
    private String lossReason;

    @Column(name = "status", length = 20)
    private String status = "PENDING"; // PENDING, COMPLETED
}
