package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
/**
 * 模块：系统管理
 * 功能：登录日志实体类，记录用户登录的IP、时间、状态等信息
 */

@Data
@Entity
@Table(name = "login_log")
public class LoginLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "ip", length = 50)
    private String ip;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "browser", length = 100)
    private String browser;

    @Column(name = "os", length = 100)
    private String os;

    @Column(name = "status")
    private Integer status;

    @Column(name = "msg", length = 200)
    private String msg;

    @CreationTimestamp
    @Column(name = "login_time", updatable = false)
    private LocalDateTime loginTime;
}
