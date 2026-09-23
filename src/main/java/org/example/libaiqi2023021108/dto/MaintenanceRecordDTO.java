package org.example.libaiqi2023021108.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MaintenanceRecordDTO {
    private Long id;
    private Long planId;
    private String planName;       // 保养计划名称
    private Long instrumentId;
    private String instrumentName; // 设备名称
    private LocalDate maintenanceDate;
    private Long maintainerId;
    private String executorName;   // 执行人姓名
    private String maintenanceContent;
    private String result;
    private String status;         // 前端兼容: 根据日期推导
    private LocalDate nextMaintenanceDate;
    private String remark;
    private LocalDateTime createTime;
}
