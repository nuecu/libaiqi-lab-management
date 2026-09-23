package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.AiKnowledge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiKnowledgeRepository extends JpaRepository<AiKnowledge, Long> {
    List<AiKnowledge> findByStatus(String status);
    Page<AiKnowledge> findByCategoryContaining(String category, Pageable pageable);
    Page<AiKnowledge> findByQuestionContainingOrKeywordsContaining(String q, String k, Pageable pageable);
    boolean existsByQuestion(String question);
}
