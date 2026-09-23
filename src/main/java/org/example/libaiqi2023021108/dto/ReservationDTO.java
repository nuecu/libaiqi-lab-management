package org.example.libaiqi2023021108.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ReservationDTO {
    private Long id;
    private String reservationNo;
    private Long userId;
    private String userName;  // 预约人姓名
    private String resourceType;
    private Long resourceId;
    private String resourceName;
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String experimentTopic;
    private Integer participantCount;
    private String participants;
    private String status;
    private String rejectReason;
    private Long approverId;
    private LocalDateTime approveTime;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String remark;
}
