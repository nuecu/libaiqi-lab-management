package org.example.libaiqi2023021108.dto;

import lombok.Data;
import java.util.List;
/** AI问答响应：答案 + 会话ID + 引用到的知识条目 + 是否越界被拒 */
@Data
public class AiChatResponse {
    private String sessionId;
    private String answer;
    private boolean outOfScope;      // 命中知识少且被判为无关问题时为 true
    private List<KbRef> refs;

    @Data
    public static class KbRef {
        private Long id;
        private String category;
        private String question;
        public KbRef() {}
        public KbRef(Long id, String category, String question) {
            this.id = id; this.category = category; this.question = question;
        }
    }
}
