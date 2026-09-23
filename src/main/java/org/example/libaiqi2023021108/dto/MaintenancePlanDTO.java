package org.example.libaiqi2023021108.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MaintenancePlanDTO {
    private Long id;
    private String planName;
    private Long instrumentId;
    private String instrumentName;  // 设备名称
    private String maintenanceType; // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    private Integer cycleDays;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;
    private Long responsibleId;
    private String status;          // ACTIVE, OVERDUE, COMPLETED
    private Boolean enabled;        // 前端兼容: status == "ACTIVE"
    private String remark;
    private LocalDateTime createTime;
}
