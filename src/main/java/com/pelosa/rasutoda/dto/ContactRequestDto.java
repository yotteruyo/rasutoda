package com.pelosa.rasutoda.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactRequestDto {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String contactEmail;

    @NotBlank(message = "문의 제목을 입력해주세요.")
    private String contactSubject;

    @NotBlank(message = "문의 내용을 입력해주세요.")
    private String contactMessage;

}
