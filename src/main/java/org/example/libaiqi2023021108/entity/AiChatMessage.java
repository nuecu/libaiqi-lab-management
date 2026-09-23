package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
/**
 * 模块：AI助手
 * 功能：AI问答对话历史，按用户与会话隔离存储
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ai_chat_message")
public class AiChatMessage extends BaseEntity {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_id", length = 64)
    private String sessionId;

    @Column(name = "role", nullable = false, length = 20)
    private String role; // user, assistant

    @Lob
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Lob
    @Column(name = "refs", columnDefinition = "TEXT")
    private String refs; // 引用到的知识库条目(JSON)
}
