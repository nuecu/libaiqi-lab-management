package org.example.libaiqi2023021108.service;

import org.example.libaiqi2023021108.common.BusinessException;
import org.example.libaiqi2023021108.entity.AiKnowledge;
import org.example.libaiqi2023021108.repository.AiKnowledgeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
/**
 * 模块：AI助手
 * 功能：受限知识库服务——条目即AI被允许回答的全部范围；同时提供关键词检索
 */
@Service
public class AiKnowledgeService {

    private final AiKnowledgeRepository repository;

    public AiKnowledgeService(AiKnowledgeRepository repository) { this.repository = repository; }

    public Page<AiKnowledge> list(String keyword, String category, Pageable pageable) {
        if (category != null && !category.isBlank()) return repository.findByCategoryContaining(category, pageable);
        if (keyword != null && !keyword.isBlank())
            return repository.findByQuestionContainingOrKeywordsContaining(keyword, keyword, pageable);
        return repository.findAll(pageable);
    }

    public List<AiKnowledge> listAllActive() { return repository.findByStatus("ACTIVE"); }

    public AiKnowledge getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException("知识条目不存在"));
    }

    @Transactional
    public AiKnowledge create(AiKnowledge k) {
        k.setId(null);
        if (k.getQuestion() == null || k.getQuestion().isBlank()) throw new BusinessException("问题/主题不能为空");
        if (k.getStatus() == null) k.setStatus("ACTIVE");
        return repository.save(k);
    }

    @Transactional
    public AiKnowledge update(Long id, AiKnowledge k) {
        AiKnowledge e = getById(id);
        if (k.getQuestion() != null) e.setQuestion(k.getQuestion());
        if (k.getAnswer() != null) e.setAnswer(k.getAnswer());
        if (k.getKeywords() != null) e.setKeywords(k.getKeywords());
        if (k.getCategory() != null) e.setCategory(k.getCategory());
        if (k.getStatus() != null) e.setStatus(k.getStatus());
        return repository.save(e);
    }

    @Transactional
    public void delete(Long id) { repository.deleteById(id); }

    /**
     * 关键词检索：对启用的知识条目按“问题命中 + 关键词命中 + 分类/主题词命中”打分，返回得分>0的Top N。
     * 纯字符串匹配，无需向量库，离线可用。
     */
    public List<AiKnowledge> retrieve(String question, int topK) {
        if (question == null || question.isBlank()) return List.of();
        String q = question.toLowerCase();
        List<AiKnowledge> active = repository.findByStatus("ACTIVE");
        List<Scored> scored = new ArrayList<>();
        for (AiKnowledge k : active) {
            int score = 0;
            // 1) 知识条目问题里的词出现在用户问题中，或用户问题出现在条目问题里
            String kn = norm(k.getQuestion());
            if (!kn.isEmpty() && (q.contains(kn) || containsAnyToken(q, kn))) score += 3;
            // 2) 关键词命中
            if (k.getKeywords() != null) {
                for (String kw : k.getKeywords().split("[,，、 　]+")) {
                    String w = kw.trim().toLowerCase();
                    if (w.length() >= 1 && !w.isEmpty() && q.contains(w)) { score += 2; }
                }
            }
            // 3) 分类命中
            if (k.getCategory() != null && q.contains(k.getCategory().toLowerCase())) score += 1;
            // 4) 答案里出现用户问题中的较长词（兜底，弱信号）
            if (score == 0 && k.getAnswer() != null && q.length() >= 4 && k.getAnswer().toLowerCase().contains(q.substring(0, Math.min(4, q.length())))) score += 1;
            if (score > 0) scored.add(new Scored(k, score));
        }
        scored.sort(Comparator.comparingInt((Scored s) -> s.score).reversed());
        List<AiKnowledge> out = new ArrayList<>();
        for (int i = 0; i < Math.min(topK, scored.size()); i++) out.add(scored.get(i).k);
        return out;
    }

    private boolean containsAnyToken(String q, String title) {
        // 将条目问题按非中日英字符切词，任一长度>=2的词出现在用户问题中即算命中
        for (String t : title.split("[ /／、,，。（）()·-]+")) {
            if (t.length() >= 2 && q.contains(t)) return true;
        }
        return false;
    }

    private String norm(String s) { return s == null ? "" : s.trim().toLowerCase(); }

    private static class Scored { final AiKnowledge k; final int score; Scored(AiKnowledge k, int s){this.k=k;this.score=s;} }
}
