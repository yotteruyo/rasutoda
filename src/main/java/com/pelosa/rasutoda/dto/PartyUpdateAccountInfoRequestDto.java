package com.pelosa.rasutoda.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyUpdateAccountInfoRequestDto {

    @NotBlank(message = "아이디를 입력해주세요.")
    private String ottAccountId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String ottAccountPassword;

}
