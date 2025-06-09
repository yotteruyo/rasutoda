package com.pelosa.rasutoda.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    ACTIVE("ACTIVE","활성"),
    DORMANT("DORMANT","휴먼"),
    BANNED("BANNED","정지"),
    PENDING("PENDIN","승인 대기");


    private final String key;
    private final String title;
}
