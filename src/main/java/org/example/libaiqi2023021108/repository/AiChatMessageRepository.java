package org.example.libaiqi2023021108.repository;

import org.example.libaiqi2023021108.entity.AiChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {
    List<AiChatMessage> findByUserIdAndSessionIdOrderByIdAsc(Long userId, String sessionId);
    Page<AiChatMessage> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);
    void deleteByUserIdAndSessionId(Long userId, String sessionId);
}
