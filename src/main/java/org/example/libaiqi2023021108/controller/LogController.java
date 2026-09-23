package org.example.libaiqi2023021108.controller;

import org.example.libaiqi2023021108.common.Result;
import org.example.libaiqi2023021108.entity.OperationLog;
import org.example.libaiqi2023021108.entity.LoginLog;
import org.example.libaiqi2023021108.service.OperationLogService;
import org.example.libaiqi2023021108.service.AuthService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * 模块：系统管理
 * 功能：日志管理控制器
 * 说明：提供操作日志和登录日志的分页查询接口，支持按用户名、操作类型筛选
 * 权限：需要 system:log
 */
@RestController
@RequestMapping("/api/logs")
@PreAuthorize("hasAuthority('system:log')")
public class LogController {
    private final OperationLogService operationLogService;
    private final AuthService authService;

    public LogController(OperationLogService operationLogService, AuthService authService) {
        this.operationLogService = operationLogService;
        this.authService = authService;
    }

    /** 分页查询操作日志，支持按用户名和操作类型筛选 */
    @GetMapping("/operation")
    public Result<List<OperationLog>> listOperationLogs(@RequestParam(required = false) String username,
                                                         @RequestParam(required = false) String operation,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        Page<OperationLog> p = operationLogService.list(username, operation, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }

    /** 分页查询登录日志，支持按用户名筛选 */
    @GetMapping("/login")
    public Result<List<LoginLog>> listLoginLogs(@RequestParam(required = false) String username,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        Page<LoginLog> p = authService.getLoginLogs(username, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }
}
