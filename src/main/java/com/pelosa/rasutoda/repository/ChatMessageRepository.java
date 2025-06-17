package com.pelosa.rasutoda.repository;

import com.pelosa.rasutoda.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByParty_IdOrderByCreatedAtAsc(Long partyId);

    @Modifying
    @Query("DELETE FROM ChatMessage cm WHERE cm.party.id = :partyId")
    void deleteByPartyId(@Param("partyId") Long partyId);

    @Modifying
    @Query("DELETE FROM ChatMessage cm WHERE cm.party.id = :partyId AND cm.sender.id = :senderId")
    void deleteByPartyIdAndSenderId(@Param("partyId") Long partyId, @Param("senderId") Long senderId);
}