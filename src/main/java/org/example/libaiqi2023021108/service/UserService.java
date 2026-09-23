package org.example.libaiqi2023021108.service;

import org.example.libaiqi2023021108.common.BusinessException;
import org.example.libaiqi2023021108.entity.SysUser;
import org.example.libaiqi2023021108.entity.SysUserRole;
import org.example.libaiqi2023021108.repository.SysUserRepository;
import org.example.libaiqi2023021108.repository.SysUserRoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * 模块：系统管理
 * 功能：用户管理服务
 */

@Service
public class UserService {

    private final SysUserRepository userRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionResolverService permissionResolverService;

    public UserService(SysUserRepository userRepository, SysUserRoleRepository userRoleRepository,
                       PasswordEncoder passwordEncoder, PermissionResolverService permissionResolverService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.permissionResolverService = permissionResolverService;
    }

    /** 分页查询用户列表，支持按关键字（姓名/用户名）、用户类型、部门筛选 */
    public Page<SysUser> list(String keyword, String userType, String department, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return userRepository.findByRealNameContainingOrUsernameContaining(keyword, keyword, pageable);
        }
        if (userType != null && !userType.isEmpty()) {
            return userRepository.findByUserType(userType, pageable);
        }
        if (department != null && !department.isEmpty()) {
            return userRepository.findByDepartment(department, pageable);
        }
        return userRepository.findAll(pageable);
    }

    /** 根据ID查询用户，不存在则抛出异常 */
    public SysUser getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new BusinessException("用户不存在"));
    }

    @Transactional
    public SysUser create(SysUser user, List<Long> roleIds) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        SysUser saved = userRepository.save(user);
        saveUserRoles(saved.getId(), roleIds);
        permissionResolverService.evictAllUserPermissions();
        return saved;
    }

    @Transactional
    public SysUser update(Long id, SysUser user, List<Long> roleIds) {
        SysUser existing = getById(id);
        // 仅复制请求中显式提供的字段（null 视为“不修改”），避免部分更新（如 PUT /users/me）
        // 把 user_type / status 等未提交字段抹成 null，进而导致下次登录拆箱 NPE。
        if (user.getRealName() != null) existing.setRealName(user.getRealName());
        if (user.getEmail() != null) existing.setEmail(user.getEmail());
        if (user.getPhone() != null) existing.setPhone(user.getPhone());
        if (user.getDepartment() != null) existing.setDepartment(user.getDepartment());
        if (user.getMajor() != null) existing.setMajor(user.getMajor());
        if (user.getUserType() != null) existing.setUserType(user.getUserType());
        if (user.getStatus() != null) existing.setStatus(user.getStatus());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        SysUser saved = userRepository.save(existing);
        if (roleIds != null) {
            userRoleRepository.deleteByUserId(id);
            saveUserRoles(id, roleIds);
            permissionResolverService.evictAllUserPermissions();
        }
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        userRoleRepository.deleteByUserId(id);
        userRepository.deleteById(id);
        permissionResolverService.evictAllUserPermissions();
    }

    /** 重置用户密码（加密存储） */
    public void resetPassword(Long id, String newPassword) {
        SysUser user = getById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /** 修改密码：校验旧密码是否正确 */
    public void changePassword(Long id, String oldPassword, String newPassword) {
        SysUser user = getById(id);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /** 批量保存用户角色关联关系 */
    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleRepository.save(ur);
            }
        }
    }
}
