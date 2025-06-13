package com.pelosa.rasutoda.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.Pattern;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordUpdateDto {

    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    private String currentPassword; // 현재 비밀번호 필드

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8 , max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    @Pattern(regexp = ".*[!@#$%^&*(),.?\":{}|<>].*", message = "비밀번호에 특수문자를 최소 1개 이상 포함해야 합니다.")
    private String newPassword;

    @NotBlank(message = "새 비밀번호를 다시 한번 입력해주세요.")
    private String confirmNewPassword; // 새 비밀번호 확인 필드
}
