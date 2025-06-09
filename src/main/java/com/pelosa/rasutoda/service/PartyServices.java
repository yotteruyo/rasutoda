package com.pelosa.rasutoda.service;

import com.pelosa.rasutoda.domain.Party;
import com.pelosa.rasutoda.dto.PartyDto;
import com.pelosa.rasutoda.repository.PartyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyServices {

    private final PartyRepository partyRepository;

    public List<PartyDto> findAllParties() {
        return partyRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PartyDto> findPartiesByOtt(String ott) {
        return partyRepository.findByOttNameIgnoreCase(ott).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    private PartyDto convertToDto(Party party) {
        long remainingDays = 0;
        if (party.getEndDate() != null) {
            remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), party.getEndDate());
        }
        return PartyDto.builder()
                .id(party.getId())
                .ottName(party.getOtt().getName())
                .title(party.getTitle())
                .currentMembers(party.getMembers().size())
                .maxMembers(party.getMaxMembers())
                .remainingDays(remainingDays)
                .monthlyPrice(party.getMonthlyPrice())
                .build();
    }
}
