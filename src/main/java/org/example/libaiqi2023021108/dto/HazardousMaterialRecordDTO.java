package org.example.libaiqi2023021108.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HazardousMaterialRecordDTO {
    private Long id;
    private Long consumableId;
    private String materialName;  // 物品名称（从consumable获取或使用consumableName）
    private String chemicalFormula;  // 化学式
    private String dangerLevel;  // 危险等级
    private Long userId;
    private String operatorName;  // 操作人姓名
    private String operationType;  // 操作类型
    private Integer quantity;
    private String unit;  // 单位
    private Boolean dualConfirm;
    private Long confirmUser1Id;
    private Long confirmUser2Id;
    private Boolean qualificationVerified;
    private String usageTracking;
    private LocalDateTime operationTime;  // 操作时间（create_time）
    private String storageLocation;  // 存放位置
}
