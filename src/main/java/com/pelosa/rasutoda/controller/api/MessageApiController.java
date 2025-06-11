package com.pelosa.rasutoda.controller.api;


import com.pelosa.rasutoda.dto.ChatMessageDto;
import com.pelosa.rasutoda.service.PartyServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/parties/{partyId}/messages")
public class MessageApiController {

    private final PartyServices partyService;

    @GetMapping
    public ResponseEntity<List<ChatMessageDto>> getMessages(@PathVariable Long partyId) {
        List<ChatMessageDto> messages = partyService.getChatMessagesByPartyId(partyId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping // 메시지 전송
    public ResponseEntity<ChatMessageDto> postMessage(@PathVariable Long partyId, @RequestBody ChatMessageDto chatMessageDto) {
        ChatMessageDto createdMessage = partyService.saveChatMessage(partyId, chatMessageDto);
        return ResponseEntity.ok(createdMessage);
    }
}