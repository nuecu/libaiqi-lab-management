package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
/**
 * 模块：基础资源管理
 * 功能：楼宇实体类，记录楼宇名称、编码、楼层数、地址等信息
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "building")
public class Building extends BaseEntity {
    @Column(name = "building_name", nullable = false, length = 100)
    private String buildingName;

    @Column(name = "building_code", unique = true, length = 50)
    private String buildingCode;

    @Column(name = "floors")
    private Integer floors;

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "status")
    private Integer status = 1;
}
