package org.example.libaiqi2023021108.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.libaiqi2023021108.common.Result;
import org.example.libaiqi2023021108.entity.SysPermission;
import org.example.libaiqi2023021108.entity.SysUser;
import org.example.libaiqi2023021108.service.AuthService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
/**
 * 模块：系统管理
 * 功能：认证授权控制器
 * 说明：提供用户登录、注册、权限查询等认证相关接口，登录时记录登录日志并签发JWT令牌
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录
     * @param loginRequest 包含用户名和密码的请求体
     * @param request HTTP请求对象，用于获取客户端IP地址
     * @return 返回JWT令牌、用户ID、用户名、真实姓名、角色、权限等信息
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest, HttpServletRequest request) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        String ip = request.getRemoteAddr();
        return Result.success(authService.login(username, password, ip));
    }

    /**
     * 用户注册
     * @param user 注册的用户信息
     * @return 返回注册成功的用户对象
     */
    @PostMapping("/register")
    public Result<SysUser> register(@RequestBody SysUser user) {
        return Result.success(authService.register(user));
    }

    /**
     * 获取当前登录用户的权限编码列表
     * @return 权限编码列表，如 ["system", "system:user", "resource", ...]
     */
    @GetMapping("/permissions")
    public Result<List<String>> getPermissions() {
        Long userId = getCurrentUserId();
        return Result.success(authService.getPermissionCodes(userId));
    }

    /**
     * 获取当前登录用户的权限菜单树（用于侧边栏渲染）
     * @return 顶级菜单列表，每个菜单包含子菜单
     */
    @GetMapping("/menus")
    public Result<List<SysPermission>> getMenus() {
        Long userId = getCurrentUserId();
        return Result.success(authService.getPermissionMenuTree(userId));
    }

    /** 从SecurityContext获取当前登录用户ID */
    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.valueOf(principal.toString());
    }
}
