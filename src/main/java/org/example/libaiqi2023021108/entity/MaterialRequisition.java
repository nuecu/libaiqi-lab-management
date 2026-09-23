package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
/**
 * 模块：实验管理
 * 功能：物料领用单实体类，包含领用编号、申请人、实验项目、审批状态等
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "material_requisition")
public class MaterialRequisition extends BaseEntity {
    @Column(name = "requisition_no", unique = true, nullable = false, length = 50)
    private String requisitionNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "experiment_log_id")
    private Long experimentLogId;

    @Column(name = "experiment_project", length = 200)
    private String experimentProject;

    @Column(name = "status", length = 20)
    private String status = "PENDING"; // PENDING, APPROVED, ISSUED, REJECTED

    @Column(name = "approver_id")
    private Long approverId;

    @Column(name = "approve_time")
    private java.time.LocalDateTime approveTime;

    @Column(name = "total_items")
    private Integer totalItems = 0;
}
