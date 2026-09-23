package org.example.libaiqi2023021108.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RepairOrderDTO {
    private Long id;
    private String orderNo;
    private Long instrumentId;
    private String instrumentName;  // 设备名称
    private Long reporterId;
    private String reporterName;    // 报修人姓名
    private String faultDesc;
    private String faultImage;
    private Long assigneeId;
    private String handlerName;     // 处理人姓名
    private String repairDesc;
    private String repairResult;
    private BigDecimal repairCost;
    private String status;
    private LocalDateTime completeTime;
    private LocalDateTime reportTime; // 报修时间 (createTime)
    private String remark;
    private LocalDateTime createTime;
}
