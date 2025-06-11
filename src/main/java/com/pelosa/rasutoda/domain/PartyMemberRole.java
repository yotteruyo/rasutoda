package com.pelosa.rasutoda.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PartyMemberRole {
    LEADER("LEADER", "파티장"),
    MEMBER("MEMBER", "파티원");

    private final String key;
    private final String title;
}