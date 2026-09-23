package org.example.libaiqi2023021108.service;

import org.example.libaiqi2023021108.common.BusinessException;
import org.example.libaiqi2023021108.common.JwtUtil;
import org.example.libaiqi2023021108.entity.*;
import org.example.libaiqi2023021108.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
/**
 * 模块：系统管理
 * 功能：认证授权服务
 */

@Service
public class AuthService {

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final SysRolePermissionRepository rolePermissionRepository;
    private final SysPermissionRepository permissionRepository;
    private final LoginLogRepository loginLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(SysUserRepository userRepository, SysRoleRepository roleRepository,
                       SysUserRoleRepository userRoleRepository, SysRolePermissionRepository rolePermissionRepository,
                       SysPermissionRepository permissionRepository, LoginLogRepository loginLogRepository,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
        this.loginLogRepository = loginLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /** 用户登录验证：校验用户名密码、账号状态，签发JWT令牌并记录登录日志 */
    public Map<String, Object> login(String username, String password, String ip) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            saveLoginLog(user, username, ip, 0, "密码错误");
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            saveLoginLog(user, username, ip, 0, "账号已禁用");
            throw new BusinessException(403, "账号已禁用，请联系管理员");
        }

        // 获取用户所有角色
        List<SysUserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        List<String> roleCodes = new ArrayList<>();
        Set<Long> permissionIds = new LinkedHashSet<>();
        for (SysUserRole ur : userRoles) {
            SysRole role = roleRepository.findById(ur.getRoleId()).orElse(null);
            if (role != null) {
                roleCodes.add(role.getRoleCode());
                // 收集该角色的所有权限ID
                List<SysRolePermission> rps = rolePermissionRepository.findByRoleId(role.getId());
                for (SysRolePermission rp : rps) {
                    permissionIds.add(rp.getPermissionId());
                }
            }
        }
        String primaryRole = roleCodes.isEmpty() ? "USER" : roleCodes.get(0);

        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), primaryRole);

        saveLoginLog(user, username, ip, 1, "登录成功");

        // 查询权限详情（permissionCode列表）
        List<SysPermission> permissions = permissionRepository.findAllById(permissionIds);
        List<String> permissionCodes = permissions.stream()
                .map(SysPermission::getPermissionCode).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("userType", user.getUserType());
        result.put("role", primaryRole);
        result.put("roles", roleCodes);
        result.put("permissions", permissionCodes);
        result.put("avatar", user.getAvatar());
        return result;
    }

    @Transactional
    public SysUser register(SysUser user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        // 公开注册固定为普通学生，禁止通过 userType 传入 ADMIN/TEACHER 实现越权提权
        user.setUserType("STUDENT");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        SysUser saved = userRepository.save(user);

        SysRole defaultRole = roleRepository.findByRoleCode("STUDENT").orElse(null);
        if (defaultRole != null) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(saved.getId());
            ur.setRoleId(defaultRole.getId());
            userRoleRepository.save(ur);
        }
        // 密码不回显由 SysUser.password 上的 @JsonProperty(WRITE_ONLY) 统一保证；
        // 此处不能对受管实体置空，否则事务提交会发出 UPDATE password=null 触发非空约束。
        return saved;
    }

    /** 分页查询登录日志，支持按用户名筛选 */
    public Page<LoginLog> getLoginLogs(String username, Pageable pageable) {
        if (username != null && !username.isEmpty()) {
            return loginLogRepository.findByUsernameContaining(username, pageable);
        }
        return loginLogRepository.findAll(pageable);
    }

    /** 根据用户ID获取权限编码列表 */
    public List<String> getPermissionCodes(Long userId) {
        List<SysUserRole> userRoles = userRoleRepository.findByUserId(userId);
        Set<Long> permissionIds = new LinkedHashSet<>();
        for (SysUserRole ur : userRoles) {
            List<SysRolePermission> rps = rolePermissionRepository.findByRoleId(ur.getRoleId());
            for (SysRolePermission rp : rps) {
                permissionIds.add(rp.getPermissionId());
            }
        }
        return permissionRepository.findAllById(permissionIds).stream()
                .map(SysPermission::getPermissionCode).collect(Collectors.toList());
    }

    /** 根据用户ID获取权限菜单树（用于前端侧边栏渲染） */
    public List<SysPermission> getPermissionMenuTree(Long userId) {
        List<SysUserRole> userRoles = userRoleRepository.findByUserId(userId);
        Set<Long> permissionIds = new LinkedHashSet<>();
        for (SysUserRole ur : userRoles) {
            List<SysRolePermission> rps = rolePermissionRepository.findByRoleId(ur.getRoleId());
            for (SysRolePermission rp : rps) {
                permissionIds.add(rp.getPermissionId());
            }
        }
        List<SysPermission> allPerms = permissionRepository.findAllById(permissionIds);
        // 只返回启用的菜单，且只返回顶级菜单（parentId为0或null）
        return allPerms.stream()
                .filter(p -> p.getStatus() == 1 && "MENU".equals(p.getPermissionType()))
                .filter(p -> p.getParentId() == null || p.getParentId() == 0)
                .collect(Collectors.toList());
    }

    /** 保存登录日志记录 */
    private void saveLoginLog(SysUser user, String username, String ip, int status, String msg) {
        LoginLog log = new LoginLog();
        log.setUserId(user.getId());
        log.setUsername(username);
        log.setIp(ip);
        log.setStatus(status);
        log.setMsg(msg);
        loginLogRepository.save(log);
    }
}
