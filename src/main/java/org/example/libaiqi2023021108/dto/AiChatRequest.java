package org.example.libaiqi2023021108.dto;

import lombok.Data;
/** AI问答请求：问题 + 会话ID(可选) */
@Data
public class AiChatRequest {
    private String question;
    private String sessionId;
}
