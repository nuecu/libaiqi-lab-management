package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
/**
 * 模块：预约管理
 * 功能：预约规则实体类，定义可预约时间范围、提前天数、最大时长、审批方式等规则
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "reservation_rule")
public class ReservationRule extends BaseEntity {
    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "resource_type", length = 20)
    private String resourceType; // LABORATORY, INSTRUMENT

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "available_start_time", length = 10)
    private String availableStartTime;

    @Column(name = "available_end_time", length = 10)
    private String availableEndTime;

    @Column(name = "advance_days")
    private Integer advanceDays = 7;

    @Column(name = "max_duration_hours")
    private Integer maxDurationHours = 4;

    @Column(name = "max_times_per_day")
    private Integer maxTimesPerDay = 2;

    @Column(name = "min_qualification", length = 50)
    private String minQualification;

    @Column(name = "auto_approve")
    private Boolean autoApprove = false;

    @Column(name = "status")
    private Integer status = 1;
}
