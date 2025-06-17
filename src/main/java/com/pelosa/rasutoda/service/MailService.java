package com.pelosa.rasutoda.service;

import com.pelosa.rasutoda.dto.ContactRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender javaMailSender;

    @Value("${admin.email}")
    private String adminEmail;

    public void sendContactEmail(ContactRequestDto contactRequestDto){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(contactRequestDto.getContactEmail());
        message.setTo(adminEmail);
        message.setSubject("[1:1 문의] " + contactRequestDto.getContactSubject());
        message.setText("발신자: " + contactRequestDto.getContactEmail() + "\n" + "문의 내용:\n" + contactRequestDto.getContactMessage());

        javaMailSender.send(message);
    }
}
