package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
/**
 * 模块：设备运维
 * 功能：安全预警实体类，包含预警类型、级别、内容、关联资源、处理状态等
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "safety_alert")
public class SafetyAlert extends BaseEntity {
    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType; // INVENTORY_LOW, HAZARDOUS_OVERUSE, INSTRUMENT_OVERTIME, NO_SHOW

    @Column(name = "alert_level", length = 20)
    private String alertLevel; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "alert_content", nullable = false, length = 500)
    private String alertContent;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "related_type", length = 50)
    private String relatedType;

    @Column(name = "status", length = 20)
    private String status = "ACTIVE"; // ACTIVE, ACKNOWLEDGED, RESOLVED

    @Column(name = "resolve_time")
    private java.time.LocalDateTime resolveTime;

    @Column(name = "resolver_id")
    private Long resolverId;
}
