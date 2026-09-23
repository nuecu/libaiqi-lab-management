package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
/**
 * 模块：基础资源管理
 * 功能：工位实体类，记录工位名称、编码、所属实验室、容量等信息
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "workstation")
public class Workstation extends BaseEntity {
    @Column(name = "workstation_code", unique = true, length = 50)
    private String workstationCode;

    @Column(name = "workstation_name", nullable = false, length = 100)
    private String workstationName;

    @Column(name = "laboratory_id")
    private Long laboratoryId;

    @Column(name = "position_desc", length = 200)
    private String positionDesc;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "status")
    private Integer status = 1;
}
