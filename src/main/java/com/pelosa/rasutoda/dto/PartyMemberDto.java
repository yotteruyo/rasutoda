package com.pelosa.rasutoda.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyMemberDto {
    private Long id; // PartyMember ID
    private String nickname; // 파티원의 닉네임
    private String role; // "LEADER" or "MEMBER" (PartyMemberRole의 name())
    private boolean isCurrentUser; // 현재 로그인한 유저인지 여부
}