package org.example.libaiqi2023021108.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExperimentLogDTO {
    private Long id;
    private Long reservationId;
    private Long userId;
    private String userName;        // 实验人姓名
    private Long laboratoryId;
    private String laboratoryName;  // 实验室名称
    private String experimentTopic; // 实验名称/主题
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer actualDurationMinutes;
    private String status;
    private String qrCode;
    private LocalDateTime createTime;
    private String remark;
}
