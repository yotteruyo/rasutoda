package com.pelosa.rasutoda.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PartyMemberRole {
    LEADER("LEADER", "파티장"),
    MEMBER("MEMBER", "파티원");

    private final String key;   // DB에 저장되거나 시스템 로직에서 사용될 값
    private final String title;
}