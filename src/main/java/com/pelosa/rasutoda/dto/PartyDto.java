package com.pelosa.rasutoda.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyDto {
    private Long id;
    private String ottName;
    private String title;
    private String creatorNickname;
    private int currentMembers;
    private int maxMembers;
    private long remainingDays;
    private int monthlyPrice;
    private Long creatorId;
    private String ottAccountId;
    private String ottAccountPassword;
    private LocalDate nextPaymentDate;
    private boolean isLeader;
    public void setIsLeader(boolean isLeader) {
        this.isLeader = isLeader;}
    private List<PartyMemberDto> partyMembers;
}
