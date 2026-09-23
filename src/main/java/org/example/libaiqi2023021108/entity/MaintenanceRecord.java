package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
import java.time.LocalDate;
/**
 * 模块：设备运维
 * 功能：保养记录实体类，记录每次保养的执行日期、内容、结果等
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "maintenance_record")
public class MaintenanceRecord extends BaseEntity {
    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "instrument_id")
    private Long instrumentId;

    @Column(name = "maintenance_date")
    private LocalDate maintenanceDate;

    @Column(name = "maintainer_id")
    private Long maintainerId;

    @Column(name = "maintenance_content", columnDefinition = "TEXT")
    private String maintenanceContent;

    @Column(name = "result", length = 200)
    private String result;

    @Column(name = "next_maintenance_date")
    private LocalDate nextMaintenanceDate;
}
