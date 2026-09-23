package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
/**
 * 模块：基础资源管理
 * 功能：实验室实体类，包含实验室名称、编码、所属楼宇、面积、容量、开放时间等
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "laboratory")
public class Laboratory extends BaseEntity {
    @Column(name = "lab_name", nullable = false, length = 100)
    private String labName;

    @Column(name = "lab_code", unique = true, length = 50)
    private String labCode;

    @Column(name = "building_id")
    private Long buildingId;

    @Column(name = "floor_number")
    private Integer floorNumber;

    @Column(name = "room_number", length = 20)
    private String roomNumber;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "area_sqm", precision = 8, scale = 2)
    private java.math.BigDecimal areaSqm;

    @Column(name = "lab_type", length = 50)
    private String labType; // TEACHING, RESEARCH, TESTING

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "open_start_time", length = 10)
    private String openStartTime;

    @Column(name = "open_end_time", length = 10)
    private String openEndTime;

    @Column(name = "status")
    private Integer status = 1;
}
