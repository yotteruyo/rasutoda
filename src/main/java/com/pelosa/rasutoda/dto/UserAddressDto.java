package com.pelosa.rasutoda.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAddressDto {

    @NotBlank(message = "우편번호를 입력해주세요.")
    private String postalCode;

    @NotBlank(message = "기본주소를 입력해주세요.")
    private String addressLine1;

    @NotBlank(message = "상세주소를 입력해주세요.")
    private String addressLine2;

    private String alias;
}
