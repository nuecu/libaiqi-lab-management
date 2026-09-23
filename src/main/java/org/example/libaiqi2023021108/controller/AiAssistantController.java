package org.example.libaiqi2023021108.controller;

import org.example.libaiqi2023021108.annotation.LogAction;
import org.example.libaiqi2023021108.common.Result;
import org.example.libaiqi2023021108.common.SecurityUtils;
import org.example.libaiqi2023021108.dto.AiChatRequest;
import org.example.libaiqi2023021108.dto.AiChatResponse;
import org.example.libaiqi2023021108.entity.AiChatMessage;
import org.example.libaiqi2023021108.service.AiAssistantService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * 模块：AI助手
 * 功能：受限智能问答接口（对话 / 历史 / 清空会话）
 * 权限：ai:assistant（已授予全部角色）
 */
@RestController
@RequestMapping("/api/ai")
@PreAuthorize("hasAuthority('ROLE_AUTHENTICATED')")
public class AiAssistantController {

    private final AiAssistantService assistantService;
    public AiAssistantController(AiAssistantService assistantService) { this.assistantService = assistantService; }

    /** 提问：系统会强制把回答范围限定在本实验室管理系统 */
    @PostMapping("/chat")
    @PreAuthorize("hasAuthority('ai:assistant')")
    @LogAction("AI助手对话")
    public Result<AiChatResponse> chat(@RequestBody AiChatRequest req) {
        return Result.success(assistantService.chat(req.getQuestion(), req.getSessionId(), SecurityUtils.currentUserId()));
    }

    /** 某会话的历史消息（按当前用户隔离） */
    @GetMapping("/history")
    @PreAuthorize("hasAuthority('ai:assistant')")
    public Result<List<AiChatMessage>> history(@RequestParam String sessionId) {
        return Result.success(assistantService.history(sessionId, SecurityUtils.currentUserId()));
    }

    /** 清空当前用户在某会话的历史 */
    @DeleteMapping("/history")
    @PreAuthorize("hasAuthority('ai:assistant')")
    @LogAction("清空AI会话")
    public Result<Void> clear(@RequestParam String sessionId) {
        assistantService.clearSession(sessionId, SecurityUtils.currentUserId());
        return Result.success();
    }
}
