package com.pelosa.rasutoda.controller.api;

import com.pelosa.rasutoda.dto.ContactRequestDto;
import com.pelosa.rasutoda.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContactController {
    private final MailService mailService;

    @PostMapping("/contact")
    public ResponseEntity<Map<String, String>> sendContactEmail(
            @Valid @RequestBody ContactRequestDto contactRequestDto,BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(Map.of("message", "입력 값을 확인해주세요."));
        }
        try {
            mailService.sendContactEmail(contactRequestDto);
            return ResponseEntity.ok(Map.of("message", "문의 메일이 성공적으로 발송되었습니다."));
        } catch (Exception e){
            System.err.println("문의 메일 발송 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "메일 발송에 실패했습니다."));
        }
    }
}
