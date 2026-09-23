package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
/**
 * 模块：设备运维
 * 功能：报修工单实体类，包含工单编号、仪器、故障描述、处理人、维修状态和费用等
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "repair_order")
public class RepairOrder extends BaseEntity {
    @Column(name = "order_no", unique = true, nullable = false, length = 50)
    private String orderNo;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(name = "instrument_name", length = 200)
    private String instrumentName;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(name = "fault_desc", columnDefinition = "TEXT")
    private String faultDesc;

    @Column(name = "fault_image", length = 500)
    private String faultImage;

    @Column(name = "assignee_id")
    private Long assigneeId;

    @Column(name = "repair_desc", columnDefinition = "TEXT")
    private String repairDesc;

    @Column(name = "repair_result", length = 200)
    private String repairResult;

    @Column(name = "repair_cost", precision = 10, scale = 2)
    private java.math.BigDecimal repairCost;

    @Column(name = "status", length = 20)
    private String status = "PENDING"; // PENDING, ASSIGNED, REPAIRING, COMPLETED, CLOSED

    @Column(name = "complete_time")
    private java.time.LocalDateTime completeTime;
}
