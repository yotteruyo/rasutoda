package com.pelosa.rasutoda.repository;

import com.pelosa.rasutoda.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByParty_IdOrderByCreatedAtAsc(Long partyId);

}