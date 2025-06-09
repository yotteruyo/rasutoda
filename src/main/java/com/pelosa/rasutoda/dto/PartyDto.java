package com.pelosa.rasutoda.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartyDto {
    private Long id;
    private String ottName;
    private String title;
    private int currentMembers;
    private int maxMembers;
    private long remainingDays;
    private int monthlyPrice;
}
