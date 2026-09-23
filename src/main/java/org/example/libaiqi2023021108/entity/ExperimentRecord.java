package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
/**
 * 模块：实验管理
 * 功能：实验记录实体类，记录具体实验操作内容和数据
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "experiment_record")
public class ExperimentRecord extends BaseEntity {
    @Column(name = "experiment_log_id")
    private Long experimentLogId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "instrument_id")
    private Long instrumentId;

    @Column(name = "record_content", columnDefinition = "TEXT")
    private String recordContent;

    @Column(name = "instrument_status", length = 50)
    private String instrumentStatus;

    @Column(name = "abnormal_desc", columnDefinition = "TEXT")
    private String abnormalDesc;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "report_url", length = 500)
    private String reportUrl;
}
