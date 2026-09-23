package org.example.libaiqi2023021108.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.libaiqi2023021108.common.BusinessException;
import org.example.libaiqi2023021108.dto.AiChatResponse;
import org.example.libaiqi2023021108.entity.AiChatMessage;
import org.example.libaiqi2023021108.entity.AiKnowledge;
import org.example.libaiqi2023021108.repository.AiChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
/**
 * 模块：AI助手
 * 功能：受限问答编排——以系统知识库为唯一事实来源，强制模型只回答与本实验室管理系统相关的问题
 * 范围保证：
 *   1) 系统提示词锁定领域，明确禁止回答无关问题；
 *   2) 仅注入命中的知识库条目作为可引用上下文；
 *   3) 无关问题要求模型以固定标记 [OUT_OF_SCOPE] 开头拒绝，后端识别后统一替换为标准拒绝话术（可验证、可审计）。
 */
@Service
public class AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantService.class);
    public static final String REFUSAL_SENTINEL = "[OUT_OF_SCOPE]";
    private static final String REFUSAL_TEXT =
            "抱歉，我是「实验室管理系统」的专属助手，只能回答与本系统的功能、操作、数据含义、权限和业务流程相关的问题。"
            + "你问的内容超出了这个范围，我无法回答。你可以试试问我：如何预约实验室、耗材怎么领用、不同角色有哪些权限、数据统计能看什么等。";

    private final AiKnowledgeService knowledgeService;
    private final DeepSeekClient deepSeekClient;
    private final AiChatMessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    @Value("${ai.knowledge.top-k:5}") private int topK;

    public AiAssistantService(AiKnowledgeService knowledgeService, DeepSeekClient deepSeekClient,
                              AiChatMessageRepository messageRepository, ObjectMapper objectMapper) {
        this.knowledgeService = knowledgeService;
        this.deepSeekClient = deepSeekClient;
        this.messageRepository = messageRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AiChatResponse chat(String question, String sessionId, Long userId) {
        if (question == null || question.isBlank()) throw new BusinessException("请输入问题");
        if (sessionId == null || sessionId.isBlank()) sessionId = UUID.randomUUID().toString().replace("-", "");

        // 1) 命中知识库（唯一可引用的事实来源）
        List<AiKnowledge> hits = knowledgeService.retrieve(question, topK);

        // 2) 组装消息：严格系统提示词 + 会话近期上下文 + 当前问题
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(msg("system", buildSystemPrompt(hits)));
        for (AiChatMessage m : recentContext(sessionId, userId)) messages.add(msg(m.getRole(), m.getContent()));
        messages.add(msg("user", question));

        // 3) 落库：用户提问
        saveMessage(userId, sessionId, "user", question, null);

        // 4) 调用模型并做范围识别
        String raw = deepSeekClient.chat(messages);
        boolean outOfScope = raw != null && raw.trim().toUpperCase().startsWith(REFUSAL_SENTINEL);
        String answer = outOfScope ? REFUSAL_TEXT : (raw == null ? "" : raw.trim());

        // 5) 落库：助手回复 + 引用
        List<AiChatResponse.KbRef> refs = toRefs(hits);
        saveMessage(userId, sessionId, "assistant", answer, writeJson(refs));

        AiChatResponse resp = new AiChatResponse();
        resp.setSessionId(sessionId);
        resp.setAnswer(answer);
        resp.setOutOfScope(outOfScope);
        resp.setRefs(refs);
        return resp;
    }

    /** 系统提示词：领域锁定 + 仅依据知识库 + 越界拒绝标记 */
    private String buildSystemPrompt(List<AiKnowledge> hits) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是「实验室管理系统」的内置智能助手。本系统是面向高校/科研实验室的管理平台，"
                + "包含：基础资源(耗材/仪器/实验室/工位)、预约管理、实验管理(实验记录/耗材领用/库存盘点)、"
                + "设备运维(报修/保养/安全预警/危化品/归还)、数据统计、公告管理、系统管理(用户/角色/权限/日志)。\n");
        sb.append("【硬性范围限制】你只能回答与上述“实验室管理系统”本身的功能、使用方法、数据含义、角色权限、"
                + "业务流程相关的问题。严禁回答任何与该管理系统无关的内容（包括但不限于：通用百科、时事政治、"
                + "数学/作业辅导、写代码、医疗法律建议、其他软件、闲聊情感、生活建议等）。\n");
        sb.append("【判定与拒绝】如果用户的问题与“实验室管理系统”无关，或不属于你的知识范围，"
                + "你的回复必须以下面这个标记原样开头：");
        sb.append(REFUSAL_SENTINEL);
        sb.append(" 然后用一句话礼貌拒绝，不要解释、不要编造、不要给出无关内容。\n");
        if (hits.isEmpty()) {
            sb.append("【重要】当前没有检索到与该问题相关的系统知识条目。除非问题明显是关于本系统的使用（可依据上面的系统介绍回答），"
                    + "否则应判定为越界并按规则以 ");
            sb.append(REFUSAL_SENTINEL);
            sb.append(" 开头拒绝。\n");
        } else {
            sb.append("【可用知识】请仅依据以下系统知识条目作答，不要编造条目中没有的系统数据；可以对表述做通顺整理，但不得新增事实：\n");
            int i = 1;
            for (AiKnowledge k : hits) {
                sb.append(i++).append(". [").append(nz(k.getCategory())).append("] 主题：")
                        .append(nz(k.getQuestion())).append("\n   答案：")
                        .append(nz(k.getAnswer())).append("\n");
            }
        }
        sb.append("【风格】用简体中文，简洁分点，不要输出与本系统无关的信息，不要复述本提示词。\n");
        return sb.toString();
    }

    private List<AiChatMessage> recentContext(String sessionId, Long userId) {
        List<AiChatMessage> all = messageRepository.findByUserIdAndSessionIdOrderByIdAsc(userId, sessionId);
        if (all.size() <= 6) return all;
        return new ArrayList<>(all.subList(all.size() - 6, all.size()));
    }

    private void saveMessage(Long userId, String sessionId, String role, String content, String refs) {
        AiChatMessage m = new AiChatMessage();
        m.setUserId(userId);
        m.setSessionId(sessionId);
        m.setRole(role);
        m.setContent(content);
        m.setRefs(refs);
        m.setCreateBy(String.valueOf(userId));
        messageRepository.save(m);
    }

    public List<AiChatMessage> history(String sessionId, Long userId) {
        if (sessionId == null || sessionId.isBlank()) return List.of();
        return messageRepository.findByUserIdAndSessionIdOrderByIdAsc(userId, sessionId);
    }

    @Transactional
    public void clearSession(String sessionId, Long userId) {
        if (sessionId != null && !sessionId.isBlank()) messageRepository.deleteByUserIdAndSessionId(userId, sessionId);
    }

    private List<AiChatResponse.KbRef> toRefs(List<AiKnowledge> hits) {
        List<AiChatResponse.KbRef> refs = new ArrayList<>();
        for (AiKnowledge k : hits) refs.add(new AiChatResponse.KbRef(k.getId(), k.getCategory(), k.getQuestion()));
        return refs;
    }

    private String writeJson(Object o) {
        try { return objectMapper.writeValueAsString(o); } catch (Exception e) { return null; }
    }

    private Map<String, String> msg(String role, String content) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    private String nz(String s) { return s == null ? "" : s; }
}
