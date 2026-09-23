package org.example.libaiqi2023021108.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MaterialRequisitionDTO {
    private Long id;
    private String requisitionNo;
    private Long userId;
    private String applicantName;  // 申请人姓名
    private Long experimentLogId;
    private String experimentProject;
    private String status;
    private Long approverId;
    private LocalDateTime approveTime;
    private Integer totalItems;
    private Long laboratoryId;
    private String laboratoryName;  // 实验室名称
    private LocalDateTime applyTime;  // 申请时间（create_time）
    private List<MaterialRequisitionItemDTO> items;  // 领用明细
    
    @Data
    public static class MaterialRequisitionItemDTO {
        private Long id;
        private Long requisitionId;
        private Long consumableId;
        private String consumableName;
        private Integer requestQuantity;
        private Integer actualQuantity;
        private String unit;
        private String specification;  // 规格
    }
}
