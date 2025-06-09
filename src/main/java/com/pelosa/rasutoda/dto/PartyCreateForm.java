package com.pelosa.rasutoda.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyCreateForm {
    private String ottName;
    private String partyTitle;
    private int remainingDays;
    private int maxMembers;
    private int monthlyPrice;
    private String ottAccountId;
    private String ottAccountPassword;
}