package org.example.libaiqi2023021108.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.libaiqi2023021108.common.JwtUtil;
import org.example.libaiqi2023021108.service.PermissionResolverService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final PermissionResolverService permissionResolverService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, PermissionResolverService permissionResolverService) {
        this.jwtUtil = jwtUtil;
        this.permissionResolverService = permissionResolverService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);
            String role = jwtUtil.parseToken(token).get("role", String.class);

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            // 添加主角色
            authorities.add(new SimpleGrantedAuthority("ROLE_" + (role != null ? role : "USER")));
            // 添加通用认证角色
            authorities.add(new SimpleGrantedAuthority("ROLE_AUTHENTICATED"));
            // 添加细粒度权限码（如 system:user / reservation:list），供 @PreAuthorize("hasAuthority('xxx')") 使用
            try {
                List<String> permissionCodes = permissionResolverService.getPermissionCodes(userId);
                if (permissionCodes != null) {
                    for (String code : permissionCodes) {
                        if (StringUtils.hasText(code)) {
                            authorities.add(new SimpleGrantedAuthority(code));
                        }
                    }
                }
            } catch (Exception ex) {
                // 权限解析失败不应阻断认证本身，仅记录日志（此时方法级鉴权会按“无权限”处理）
                logger.warn("解析用户权限码失败, userId=" + userId + ", 原因: " + ex.getMessage());
            }

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
