package org.example.libaiqi2023021108.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LaboratoryDTO {
    private Long id;
    private String labName;
    private String labCode;
    private Long buildingId;
    private String buildingName;  // 所属楼栋名称
    private Integer floorNumber;
    private String roomNumber;
    private Integer capacity;
    private BigDecimal areaSqm;
    private String labType;
    private Long managerId;
    private String openStartTime;
    private String openEndTime;
    private Integer status;
    private String remark;
}
