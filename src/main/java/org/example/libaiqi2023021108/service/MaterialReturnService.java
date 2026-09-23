package org.example.libaiqi2023021108.service;

import org.example.libaiqi2023021108.common.BusinessException;
import org.example.libaiqi2023021108.common.SecurityUtils;
import org.example.libaiqi2023021108.dto.MaterialReturnDTO;
import org.example.libaiqi2023021108.entity.Consumable;
import org.example.libaiqi2023021108.entity.MaterialReturn;
import org.example.libaiqi2023021108.repository.ConsumableRepository;
import org.example.libaiqi2023021108.repository.MaterialReturnRepository;
import org.example.libaiqi2023021108.repository.SysUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
/**
 * 模块：设备运维
 * 功能：耗材归还服务
 */

@Service
public class MaterialReturnService {

    private final MaterialReturnRepository returnRepository;
    private final ConsumableRepository consumableRepository;
    private final SysUserRepository userRepository;

    public MaterialReturnService(MaterialReturnRepository returnRepository,
                                  ConsumableRepository consumableRepository,
                                  SysUserRepository userRepository) {
        this.returnRepository = returnRepository;
        this.consumableRepository = consumableRepository;
        this.userRepository = userRepository;
    }

    public Page<MaterialReturn> list(Long userId, String status, Pageable pageable) {
        if (userId != null) return returnRepository.findByUserId(userId, pageable);
        if (status != null && !status.isBlank()) return returnRepository.findByStatus(status, pageable);
        return returnRepository.findAll(pageable);
    }

    /**
     * 返回包含关联信息的DTO列表
     */
    /** 查询归还记录列表（DTO格式，含归还人/接收人姓名） */
    public Page<MaterialReturnDTO> listWithDetails(Long userId, String status, Pageable pageable) {
        Page<MaterialReturn> page = list(userId, status, pageable);
        return page.map(this::convertToDTO);
    }

    /** 将归还记录转换为DTO，补充归还人和接收人姓名 */
    private MaterialReturnDTO convertToDTO(MaterialReturn r) {
        MaterialReturnDTO dto = new MaterialReturnDTO();
        dto.setId(r.getId());
        dto.setReturnNo(r.getReturnNo());
        dto.setUserId(r.getUserId());
        dto.setConsumableId(r.getConsumableId());
        dto.setConsumableName(r.getConsumableName());
        dto.setReturnQuantity(r.getReturnQuantity());
        dto.setReturnType(r.getReturnType());
        dto.setStatus(r.getStatus());
        dto.setReceiverId(r.getReceiverId());
        dto.setCreateTime(r.getCreateTime());
        // 获取归还人姓名
        userRepository.findById(r.getUserId()).ifPresent(user ->
            dto.setUserName(user.getRealName() != null ? user.getRealName() : user.getUsername())
        );
        // 获取接收人姓名
        if (r.getReceiverId() != null) {
            userRepository.findById(r.getReceiverId()).ifPresent(user ->
                dto.setReceiverName(user.getRealName() != null ? user.getRealName() : user.getUsername())
            );
        }
        return dto;
    }

    public MaterialReturn getById(Long id) {
        return returnRepository.findById(id).orElseThrow(() -> new BusinessException("归还记录不存在"));
    }

    @Transactional
    public MaterialReturn create(MaterialReturn r) {
        // 生成归还单号
        r.setReturnNo("RTN" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        if (r.getStatus() == null) r.setStatus("PENDING");
        // 前端归还表单不提交 userId，而 user_id 为非空列，缺省会触发 500；默认归属当前登录用户
        if (r.getUserId() == null) r.setUserId(SecurityUtils.currentUserId());
        // 获取耗材名称
        if (r.getConsumableId() != null) {
            consumableRepository.findById(r.getConsumableId()).ifPresent(c -> {
                if (r.getConsumableName() == null) r.setConsumableName(c.getConsumableName());
            });
        }
        return returnRepository.save(r);
    }

    @Transactional
    public MaterialReturn complete(Long id, Long receiverId) {
        MaterialReturn r = getById(id);
        r.setStatus("COMPLETED");
        r.setReceiverId(receiverId != null ? receiverId : SecurityUtils.currentUserId());
        // 恢复库存
        consumableRepository.findById(r.getConsumableId()).ifPresent(c -> {
            c.setCurrentStock(c.getCurrentStock() + r.getReturnQuantity());
            consumableRepository.save(c);
        });
        return returnRepository.save(r);
    }

    public void delete(Long id) { returnRepository.deleteById(id); }
}
