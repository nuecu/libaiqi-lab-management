package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
/**
 * 模块：设备运维
 * 功能：危化品操作记录实体类，记录危化品的操作类型、数量、双人确认等信息
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "hazardous_material_record")
public class HazardousMaterialRecord extends BaseEntity {
    @Column(name = "consumable_id", nullable = false)
    private Long consumableId;

    @Column(name = "consumable_name", length = 200)
    private String consumableName;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "operation_type", length = 20)
    private String operationType; // ISSUE, RETURN, DISPOSE

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "dual_confirm")
    private Boolean dualConfirm = false;

    @Column(name = "confirm_user1_id")
    private Long confirmUser1Id;

    @Column(name = "confirm_user2_id")
    private Long confirmUser2Id;

    @Column(name = "qualification_verified")
    private Boolean qualificationVerified = false;

    @Column(name = "usage_tracking", columnDefinition = "TEXT")
    private String usageTracking;
}
