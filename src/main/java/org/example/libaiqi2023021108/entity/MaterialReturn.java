package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
/**
 * 模块：设备运维
 * 功能：物料归还实体类，包含归还编号、归还人、耗材、归还数量、接收人等
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "material_return")
public class MaterialReturn extends BaseEntity {
    @Column(name = "return_no", unique = true, nullable = false, length = 50)
    private String returnNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "consumable_id", nullable = false)
    private Long consumableId;

    @Column(name = "consumable_name", length = 200)
    private String consumableName;

    @Column(name = "return_quantity", nullable = false)
    private Integer returnQuantity;

    @Column(name = "return_type", length = 20)
    private String returnType; // REMAINING, UNUSED

    @Column(name = "status", length = 20)
    private String status = "PENDING"; // PENDING, COMPLETED

    @Column(name = "receiver_id")
    private Long receiverId;
}
