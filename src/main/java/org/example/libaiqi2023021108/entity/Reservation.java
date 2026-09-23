package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
import java.time.LocalDate;
import java.time.LocalTime;
/**
 * 模块：预约管理
 * 功能：预约记录实体类，包含预约编号、资源信息、时间段、实验主题、审批状态等
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "reservation")
public class Reservation extends BaseEntity {
    @Column(name = "reservation_no", unique = true, nullable = false, length = 50)
    private String reservationNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "resource_type", nullable = false, length = 20)
    private String resourceType; // LABORATORY, INSTRUMENT

    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @Column(name = "resource_name", length = 200)
    private String resourceName;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "experiment_topic", length = 200)
    private String experimentTopic;

    @Column(name = "participant_count")
    private Integer participantCount = 1;

    @Column(name = "participants", columnDefinition = "TEXT")
    private String participants;

    @Column(name = "status", length = 20)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, CANCELLED, COMPLETED

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "approver_id")
    private Long approverId;

    @Column(name = "approve_time")
    private java.time.LocalDateTime approveTime;

    @Column(name = "check_in_time")
    private java.time.LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private java.time.LocalDateTime checkOutTime;
}
