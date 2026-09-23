package org.example.libaiqi2023021108.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InventoryCheckDTO {
    private Long id;
    private String checkNo;
    private String checkName;
    private LocalDate checkDate;
    private Long checkerId;
    private String checkerName;     // 盘点人姓名
    private Long consumableId;
    private String consumableName;  // 耗材名称
    private Integer systemQuantity;
    private Integer actualQuantity;
    private Integer difference;
    private String lossReason;
    private String status;
    private String remark;
    private LocalDateTime createTime;
}
