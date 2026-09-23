package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
/**
 * 模块：实验管理
 * 功能：临时领用单实体类，用于非实验关联的临时耗材领用
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "temporary_requisition")
public class TemporaryRequisition extends BaseEntity {
    @Column(name = "requisition_no", unique = true, nullable = false, length = 50)
    private String requisitionNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "consumable_id", nullable = false)
    private Long consumableId;

    @Column(name = "consumable_name", length = 200)
    private String consumableName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "status", length = 20)
    private String status = "PENDING"; // PENDING, APPROVED, ISSUED, REJECTED

    @Column(name = "approver_id")
    private Long approverId;

    @Column(name = "approve_time")
    private java.time.LocalDateTime approveTime;
}
