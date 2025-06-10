package com.pelosa.rasutoda.service;

import com.pelosa.rasutoda.domain.Party;
import com.pelosa.rasutoda.domain.Ott;
import com.pelosa.rasutoda.domain.PartyMember;
import com.pelosa.rasutoda.domain.PartyMemberRole;
import com.pelosa.rasutoda.domain.User;


import com.pelosa.rasutoda.dto.PartyDto;
import com.pelosa.rasutoda.dto.PartyCreateForm;
import com.pelosa.rasutoda.dto.PartyMemberDto;

import com.pelosa.rasutoda.repository.OttRepository;
import com.pelosa.rasutoda.repository.PartyMemberRepository;
import com.pelosa.rasutoda.repository.UserRepository;
import com.pelosa.rasutoda.repository.PartyRepository;

import com.pelosa.rasutoda.util.AES256Util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication; // 현재 로그인 유저 정보 가져오기 위함
import org.springframework.security.core.context.SecurityContextHolder; // 현재 로그인 유저 정보 가져오기 위함
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyServices {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final PartyRepository partyRepository;
    private final OttRepository ottRepository;
    private final PartyMemberRepository partyMemberRepository;
    private final AES256Util aes256Util;

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

    public List<PartyDto> findCreatedPartiesByCreator(User creator) {
        return partyRepository.findByCreator(creator).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PartyDto> findJoinedPartiesByUser(User user){
        return partyMemberRepository.findByMember(user).stream()
                .map(PartyMember::getParty)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<PartyDto> findPartyDetailById(Long partyId){
        return partyRepository.findById(partyId)
                .map(this::convertToDto);
    }

    public Optional<PartyDto> findMyPartyDetailForUser(Long partyId, User currentUser){
        return partyRepository.findById(partyId)
                .map(party -> {
                    boolean isMemberOfThisParty = party.getMembers().stream()
                            .anyMatch(pm -> pm.getMember().getId().equals(currentUser.getId()));
                    if (!isMemberOfThisParty) {
                        return null;
                    }

                    PartyDto dto = convertToDto(party);

                    try {
                        dto.setOttAccountId(party.getOttAccountId());
                        dto.setOttAccountPassword(aes256Util.decrypt(party.getOttAccountPassword())); // 복호화된 평문 비밀번호 설정
                    } catch (Exception e) {
                        System.err.println("OTT 계정 비밀번호 복호화 실패: " + e.getMessage());
                        dto.setOttAccountPassword("복호화 오류"); // 또는 null
                    }
                    LocalDate nextPaymentDate = party.getEndDate() != null ? party.getEndDate().plusMonths(1) : LocalDate.now().plusMonths(1);
                    dto.setNextPaymentDate(nextPaymentDate);

                    dto.setIsLeader(party.getCreator().getId().equals(currentUser.getId()));

                    List<PartyMemberDto> memberDtos = party.getMembers().stream()
                            .map(pm -> new PartyMemberDto(
                                    pm.getId(),
                                    pm.getMember().getNickname(),
                                    pm.getRole().name(),
                                    pm.getMember().getId().equals(currentUser.getId())
                            ))
                            .collect(Collectors.toList());
                    dto.setPartyMembers(memberDtos);

                    return dto;
                });
    }

    @Transactional
    public void createParty(PartyCreateForm form){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName(); // CustomUserDetailsService에서 설정한 loginId
        User creator = userRepository.findByLoginId(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("현재 로그인된 사용자를 찾을 수 없습니다."));
        Ott ott = ottRepository.findByNameIgnoreCase(form.getOttName()) // <-- OttRepository 사용
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 OTT 서비스입니다: " + form.getOttName()));

        String encryptedOttAccountPassword;
        try {
            encryptedOttAccountPassword = aes256Util.encrypt(form.getOttAccountPassword());
        } catch (Exception e) {
            throw new IllegalStateException("OTT 계정 비밀번호 암호화에 실패했습니다.", e);
        }


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
        Party savedParty = partyRepository.save(party);

        PartyMember initialMember = PartyMember.builder()
                .party(savedParty)
                .member(creator)
                .joinDate(LocalDate.now())
                .role(PartyMemberRole.LEADER)
                .build();

        partyMemberRepository.save(initialMember);
            savedParty.getMembers().add(initialMember);


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
                .creatorId(party.getCreator() != null ? party.getCreator().getId() : null)
                .creatorNickname(party.getCreator() != null ? party.getCreator().getNickname() : "알 수 없음")
                .monthlyPrice(party.getMonthlyPrice())
                .build();
    }
}
