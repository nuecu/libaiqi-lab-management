package org.example.libaiqi2023021108.controller;

import org.example.libaiqi2023021108.common.JwtUtil;
import org.example.libaiqi2023021108.common.Result;
import org.example.libaiqi2023021108.entity.SysUser;
import org.example.libaiqi2023021108.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 模块：系统管理
 * 功能：用户管理控制器
 * 说明：提供用户的增删改查、个人信息修改、密码重置等接口，支持按关键字、用户类型、部门筛选
 * 权限：用户管理类接口需要 system:user；/me 相关接口是“查自己/改自己”，只要求登录即可
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAuthority('system:user')")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /** 获取当前登录用户完整信息（只要求登录，任何角色都能查自己） */
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_AUTHENTICATED')")
    public Result<SysUser> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        SysUser user = userService.getById(userId);
        user.setPassword(null); // 不返回密码
        return Result.success(user);
    }

    /** 修改当前登录用户个人信息（只要求登录，任何角色都能改自己） */
    @PutMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_AUTHENTICATED')")
    public Result<SysUser> updateCurrentUser(@RequestHeader("Authorization") String authHeader,
                                             @RequestBody Map<String, Object> body) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        SysUser user = new SysUser();
        user.setRealName((String) body.get("realName"));
        user.setEmail((String) body.get("email"));
        user.setPhone((String) body.get("phone"));
        user.setDepartment((String) body.get("department"));
        user.setMajor((String) body.get("major"));
        return Result.success(userService.update(userId, user, null));
    }

    /** 修改当前登录用户密码（需验证旧密码，只要求登录） */
    @PutMapping("/me/password")
    @PreAuthorize("hasAuthority('ROLE_AUTHENTICATED')")
    public Result<Void> changePassword(@RequestHeader("Authorization") String authHeader,
                                       @RequestBody Map<String, String> body) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        userService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return Result.success();
    }

    /** 分页查询用户列表，支持按姓名/用户名模糊搜索、用户类型、部门筛选 */
    @GetMapping
    public Result<List<SysUser>> list(@RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String userType,
                                      @RequestParam(required = false) String department,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        Page<SysUser> p = userService.list(keyword, userType, department, PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.success(p.getContent(), p.getTotalElements());
    }

    /** 根据ID查询单个用户详情 */
    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) { return Result.success(userService.getById(id)); }

    /** 创建新用户，支持同时分配角色 */
    @PostMapping
    public Result<SysUser> create(@RequestBody Map<String, Object> body) {
        SysUser user = new SysUser();
        user.setUsername((String) body.get("username")); user.setPassword((String) body.get("password"));
        user.setRealName((String) body.get("realName")); user.setEmail((String) body.get("email"));
        user.setPhone((String) body.get("phone")); user.setDepartment((String) body.get("department"));
        user.setMajor((String) body.get("major")); user.setUserType((String) body.get("userType"));
        user.setStudentNo((String) body.get("studentNo")); user.setStaffNo((String) body.get("staffNo"));
        @SuppressWarnings("unchecked")
        List<Long> roleIds = body.get("roleIds") != null ? ((List<Number>) body.get("roleIds")).stream().map(Number::longValue).toList() : null;
        return Result.success(userService.create(user, roleIds));
    }

    /** 更新用户信息及角色分配 */
    @PutMapping("/{id}")
    public Result<SysUser> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SysUser user = new SysUser();
        user.setRealName((String) body.get("realName")); user.setEmail((String) body.get("email"));
        user.setPhone((String) body.get("phone")); user.setDepartment((String) body.get("department"));
        user.setMajor((String) body.get("major")); user.setUserType((String) body.get("userType"));
        user.setStatus(body.get("status") != null ? (Integer) body.get("status") : 1);
        user.setPassword((String) body.get("password"));
        @SuppressWarnings("unchecked")
        List<Long> roleIds = body.get("roleIds") != null ? ((List<Number>) body.get("roleIds")).stream().map(Number::longValue).toList() : null;
        return Result.success(userService.update(id, user, roleIds));
    }

    /** 删除用户并清除关联的角色关系 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { userService.delete(id); return Result.success(); }

    /** 管理员重置指定用户的密码 */
    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        userService.resetPassword(id, body.get("newPassword")); return Result.success();
    }
}
