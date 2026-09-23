package org.example.libaiqi2023021108.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.libaiqi2023021108.annotation.LogAction;
import org.example.libaiqi2023021108.common.IpUtils;
import org.example.libaiqi2023021108.entity.OperationLog;
import org.example.libaiqi2023021108.entity.SysUser;
import org.example.libaiqi2023021108.repository.SysUserRepository;
import org.example.libaiqi2023021108.service.OperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.stream.Collectors;
/**
 * 模块：系统管理
 * 功能：操作日志切面，自动记录所有控制器写操作（新增/修改/删除）到 operation_log 表
 * 说明：以 AOP 环绕通知统一采集操作人、请求方法、参数、IP、耗时与成功/失败，业务代码零侵入
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);
    private static final int MAX_PARAM_LEN = 1000;

    private final OperationLogService operationLogService;
    private final SysUserRepository userRepository;
    private final ObjectMapper objectMapper;

    public OperationLogAspect(OperationLogService operationLogService, SysUserRepository userRepository,
                              ObjectMapper objectMapper) {
        this.operationLogService = operationLogService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    /** 环绕所有被 @LogAction 标注，或映射为 POST/PUT/DELETE/PATCH 的控制器方法 */
    @Around("within(org.example.libaiqi2023021108.controller..*) && ("
            + "@annotation(org.springframework.web.bind.annotation.PostMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.PutMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.PatchMapping) || "
            + "@annotation(org.example.libaiqi2023021108.annotation.LogAction))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result;
        int status = 1;
        String errorMsg = null;
        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable ex) {
            status = 0;
            errorMsg = ex.getMessage();
            throw ex;
        } finally {
            try {
                record(pjp, System.currentTimeMillis() - start, status, errorMsg);
            } catch (Exception ignore) {
                // 记录日志失败绝不能影响主业务
                log.warn("写入操作日志失败: {}", ignore.getMessage());
            }
        }
    }

    private void record(ProceedingJoinPoint pjp, long cost, int status, String errorMsg) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return;
        HttpServletRequest request = attrs.getRequest();
        String uri = request.getRequestURI();
        // 跳过登录/鉴权相关接口，避免认证噪声与敏感信息落库
        if (uri != null && uri.startsWith("/api/auth")) return;

        OperationLog entity = new OperationLog();
        entity.setCostTime(cost);
        entity.setStatus(status);
        entity.setErrorMsg(truncate(errorMsg, MAX_PARAM_LEN));
        entity.setIp(IpUtils.getIp(request));

        // 当前登录用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            entity.setUserId(userId);
            userRepository.findById(userId).ifPresent(u ->
                entity.setUsername(u.getRealName() != null && !u.getRealName().isBlank()
                        ? u.getRealName() : u.getUsername()));
        }

        // 操作名称：优先取 @LogAction 注解，否则按 HTTP 方法归类
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        LogAction ann = sig.getMethod().getAnnotation(LogAction.class);
        String httpMethod = request.getMethod();
        String operation = ann != null && !ann.value().isBlank() ? ann.value() : labelOf(httpMethod);
        entity.setOperation(operation);
        entity.setMethod(httpMethod + " " + uri + " -> " + sig.getDeclaringType().getSimpleName()
                + "#" + sig.getName());
        entity.setParams(serializeArgs(pjp.getArgs()));

        operationLogService.saveAsync(entity);
    }

    private String labelOf(String httpMethod) {
        if (httpMethod == null) return "操作";
        switch (httpMethod.toUpperCase()) {
            case "POST": return "新增";
            case "PUT": return "修改";
            case "PATCH": return "更新";
            case "DELETE": return "删除";
            default: return "操作";
        }
    }

    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) return null;
        try {
            Object[] filtered = Arrays.stream(args)
                    .filter(a -> !(a instanceof HttpServletRequest) && !(a instanceof HttpServletResponse)
                            && !(a instanceof MultipartFile))
                    .toArray();
            if (filtered.length == 0) return null;
            String json = objectMapper.writeValueAsString(filtered);
            return truncate(json, MAX_PARAM_LEN);
        } catch (Exception e) {
            // 参数含不可序列化对象时退化为类型名列表
            return Arrays.stream(args).map(a -> a == null ? "null" : a.getClass().getSimpleName())
                    .collect(Collectors.joining(", "));
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
