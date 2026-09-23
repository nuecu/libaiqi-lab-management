package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
/**
 * 模块：系统管理
 * 功能：系统用户实体类，存储用户账号、密码、姓名、联系方式、部门等基本信息
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_user")
public class SysUser extends BaseEntity {
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 200)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // 可反序列化写入，永不出现在任何响应 JSON 中（防止密码/哈希回显）
    private String password;

    @Column(name = "real_name", length = 50)
    private String realName;

    @Column(name = "student_no", length = 50)
    private String studentNo;

    @Column(name = "staff_no", length = 50)
    private String staffNo;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "avatar", length = 500)
    private String avatar;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "major", length = 100)
    private String major;

    @Column(name = "user_type", length = 20)
    private String userType; // ADMIN, TEACHER, STUDENT, MAINTAINER

    @Column(name = "status")
    private Integer status = 1; // 1-启用 0-禁用

    @Column(name = "last_login_time")
    private java.time.LocalDateTime lastLoginTime;
}
