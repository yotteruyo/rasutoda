package com.pelosa.rasutoda.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateDto {

    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickname;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "휴대전화 번호를 입력해주세요.")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "010-1234-5678 형식으로 입력해주세요.")
    private String phoneNumber;
}
