package com.pelosa.rasutoda.service;

import com.pelosa.rasutoda.domain.Party;
import com.pelosa.rasutoda.domain.Ott;
import com.pelosa.rasutoda.domain.PartyMember;
import com.pelosa.rasutoda.domain.User;
import com.pelosa.rasutoda.repository.OttRepository;

import com.pelosa.rasutoda.dto.PartyDto;
import com.pelosa.rasutoda.dto.PartyCreateForm;

import com.pelosa.rasutoda.repository.OttRepository;
import com.pelosa.rasutoda.repository.UserRepository;
import com.pelosa.rasutoda.repository.PartyRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication; // 현재 로그인 유저 정보 가져오기 위함
import org.springframework.security.core.context.SecurityContextHolder; // 현재 로그인 유저 정보 가져오기 위함
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyServices {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final PartyRepository partyRepository;
    private final OttRepository ottRepository;

    public List<PartyDto> findAllParties() {
        return partyRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PartyDto> findPartiesByOtt(String ottName) {
        return partyRepository.findByOttNameIgnoreCase(ottName).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createParty(PartyCreateForm form){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName(); // CustomUserDetailsService에서 설정한 loginId
        User creator = userRepository.findByLoginId(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("현재 로그인된 사용자를 찾을 수 없습니다."));
        Ott ott = ottRepository.findByNameIgnoreCase(form.getOttName()) // <-- OttRepository 사용
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 OTT 서비스입니다: " + form.getOttName()));


        String encryptedOttAccountPassword = passwordEncoder.encode(form.getOttAccountPassword());


        Party party = Party.builder()
                .ott(ott) // Ott 엔티티 또는 Enum
                .title(form.getPartyTitle())
                .maxMembers(form.getMaxMembers())
                .currentMembers(1)
                .monthlyPrice(form.getMonthlyPrice())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(form.getRemainingDays()))
                .ottAccountId(form.getOttAccountId())
                .ottAccountPassword(encryptedOttAccountPassword)
                .creator(creator)
                .build();

        partyRepository.save(party);
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
