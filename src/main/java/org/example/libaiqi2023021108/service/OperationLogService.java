package org.example.libaiqi2023021108.service;

import org.example.libaiqi2023021108.entity.OperationLog;
import org.example.libaiqi2023021108.repository.OperationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
/**
 * 模块：系统管理
 * 功能：操作日志服务
 */

@Service
public class OperationLogService {

    private static final Logger log = LoggerFactory.getLogger(OperationLogService.class);

    private final OperationLogRepository logRepository;

    public OperationLogService(OperationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    /** 分页查询操作日志，支持按用户名和操作类型筛选 */
    public Page<OperationLog> list(String username, String operation, Pageable pageable) {
        if (username != null && !username.isEmpty()) {
            return logRepository.findByUsernameContaining(username, pageable);
        }
        if (operation != null && !operation.isEmpty()) {
            return logRepository.findByOperationContaining(operation, pageable);
        }
        return logRepository.findAll(pageable);
    }

    /** 保存操作日志 */
    public void save(OperationLog log) {
        logRepository.save(log);
    }

    /** 异步保存操作日志：由 AOP 切面调用，避免阻塞/影响主业务流程 */
    @Async
    public void saveAsync(OperationLog log) {
        try {
            logRepository.save(log);
        } catch (Exception e) {
            OperationLogService.log.warn("异步写入操作日志失败: {}", e.getMessage());
        }
    }
}
