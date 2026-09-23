package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
import java.time.LocalDateTime;
/**
 * 模块：实验管理
 * 功能：实验日志实体类，记录实验预约ID、实验人、实验室、主题、签到签退时间等
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "experiment_log")
public class ExperimentLog extends BaseEntity {
    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "laboratory_id")
    private Long laboratoryId;

    @Column(name = "experiment_topic", length = 200)
    private String experimentTopic;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "actual_duration_minutes")
    private Integer actualDurationMinutes;

    @Column(name = "status", length = 20)
    private String status = "IN_PROGRESS"; // IN_PROGRESS, PAUSED, COMPLETED

    @Column(name = "qr_code", length = 100)
    private String qrCode;
}
