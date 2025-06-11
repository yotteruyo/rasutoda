package com.pelosa.rasutoda.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {
    TEXT,
    FILE,
    SYSTEM
}
