package com.pelosa.rasutoda.dto;

import com.pelosa.rasutoda.domain.ChatMessage;
import com.pelosa.rasutoda.domain.MessageType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessageDto {
    private Long id;
    private String message;
    private String senderNickname;
    private Long senderId;
    private MessageType messageType;
    private LocalDateTime createdAt;

    public static ChatMessageDto fromEntity(ChatMessage entity) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(entity.getId());
        dto.setMessage(entity.getMessage());
        if (entity.getSender() != null) {
            dto.setSenderNickname(entity.getSender().getNickname());
            dto.setSenderId(entity.getSender().getId());
        }
        dto.setMessageType(entity.getMessageType());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}