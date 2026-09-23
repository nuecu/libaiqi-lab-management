package org.example.libaiqi2023021108.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具：从 Spring Security 上下文中获取当前登录用户 ID。
 * JwtAuthenticationFilter 已将 principal 设置为 Long 类型的 userId。
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            throw new BusinessException(401, "未登录或登录状态已过期");
        }
        return (Long) auth.getPrincipal();
    }
}
