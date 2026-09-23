package org.example.libaiqi2023021108.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.libaiqi2023021108.common.BaseEntity;
/**
 * 模块：AI助手
 * 功能：受限知识库条目——定义AI被允许回答的范围与标准答案
 * 说明：AI 只能基于本表中启用的条目回答与本实验室管理系统相关的问题
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ai_knowledge")
public class AiKnowledge extends BaseEntity {
    @Column(name = "question", nullable = false, length = 500)
    private String question;

    @Lob
    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Column(name = "keywords", length = 500)
    private String keywords;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "status", length = 20)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE
}
