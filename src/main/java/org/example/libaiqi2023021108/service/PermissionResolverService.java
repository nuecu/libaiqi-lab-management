package org.example.libaiqi2023021108.service;

import org.example.libaiqi2023021108.entity.SysPermission;
import org.example.libaiqi2023021108.entity.SysRolePermission;
import org.example.libaiqi2023021108.entity.SysUserRole;
import org.example.libaiqi2023021108.repository.SysPermissionRepository;
import org.example.libaiqi2023021108.repository.SysRolePermissionRepository;
import org.example.libaiqi2023021108.repository.SysUserRoleRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 模块：系统管理
 * 功能：权限码解析服务
 *
 * 说明：JwtAuthenticationFilter 需要在每个请求上把「细粒度权限码」装进 SecurityContext，
 *      才能让 @PreAuthorize("hasAuthority('system:user')") 这类方法级鉴权生效。
 *      这里只依赖三个 Repository，刻意不依赖 AuthService，
 *      否则会形成 SecurityConfig -> JwtAuthenticationFilter -> AuthService -> PasswordEncoder(由 SecurityConfig 提供) 的循环依赖。
 *
 * 性能：结果按 userId 缓存到 Redis（cache 名 userPermissions），避免每个请求都查三次库。
 *      角色/权限变更后不强制失效也可以接受：缓存随 TTL（1 小时）自然过期，重新登录同样会命中已有缓存，
 *      如需立即生效可重启应用或清理 Redis 中 userPermissions::<userId> 键。
 */
@Service
public class PermissionResolverService {

    private final SysUserRoleRepository userRoleRepository;
    private final SysRolePermissionRepository rolePermissionRepository;
    private final SysPermissionRepository permissionRepository;

    public PermissionResolverService(SysUserRoleRepository userRoleRepository,
                                     SysRolePermissionRepository rolePermissionRepository,
                                     SysPermissionRepository permissionRepository) {
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    /** 四级联查：用户 -> 用户角色 -> 角色权限 -> 权限，返回去重后的 permissionCode 列表 */
    @Cacheable(value = "userPermissions", key = "#userId")
    public List<String> getPermissionCodes(Long userId) {
        if (userId == null) return List.of();
        List<SysUserRole> userRoles = userRoleRepository.findByUserId(userId);
        Set<Long> permissionIds = new LinkedHashSet<>();
        for (SysUserRole ur : userRoles) {
            List<SysRolePermission> rps = rolePermissionRepository.findByRoleId(ur.getRoleId());
            for (SysRolePermission rp : rps) {
                permissionIds.add(rp.getPermissionId());
            }
        }
        return permissionRepository.findAllById(permissionIds).stream()
                .map(SysPermission::getPermissionCode)
                .filter(code -> code != null && !code.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 清空 userPermissions 缓存。
     * 修复点：角色权限、权限定义、用户角色发生变更后，各用户已缓存的权限码会立即失效，
     * 下次请求重新回源解析，避免被降权用户在 TTL 内仍持有旧权限（越权）或新授权迟迟不生效。
     */
    @CacheEvict(value = "userPermissions", allEntries = true)
    public void evictAllUserPermissions() {
        // 仅用于触发缓存失效，无业务逻辑
    }
}
