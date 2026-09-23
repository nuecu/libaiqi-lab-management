package org.example.libaiqi2023021108.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MaterialReturnDTO {
    private Long id;
    private String returnNo;
    private Long userId;
    private String userName;  // 归还人姓名
    private Long consumableId;
    private String consumableName;
    private Integer returnQuantity;
    private String returnType;
    private String status;
    private Long receiverId;
    private String receiverName;  // 接收人姓名
    private LocalDateTime createTime;
}
