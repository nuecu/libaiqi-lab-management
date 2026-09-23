package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
import java.time.LocalDate;
/**
 * 模块：设备运维
 * 功能：保养计划实体类，定义仪器的定期保养周期、负责人、上次/下次保养日期
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "maintenance_plan")
public class MaintenancePlan extends BaseEntity {
    @Column(name = "plan_name", nullable = false, length = 200)
    private String planName;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(name = "maintenance_type", length = 50)
    private String maintenanceType; // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY

    @Column(name = "cycle_days")
    private Integer cycleDays;

    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    @Column(name = "next_maintenance_date")
    private LocalDate nextMaintenanceDate;

    @Column(name = "responsible_id")
    private Long responsibleId;

    @Column(name = "status", length = 20)
    private String status = "ACTIVE"; // ACTIVE, OVERDUE, COMPLETED
}
