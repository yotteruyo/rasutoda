package com.pelosa.rasutoda.service;

import com.pelosa.rasutoda.domain.Party;
import com.pelosa.rasutoda.domain.Ott;
import com.pelosa.rasutoda.domain.PartyMember;
import com.pelosa.rasutoda.domain.PartyMemberRole;
import com.pelosa.rasutoda.domain.User;
import com.pelosa.rasutoda.domain.ChatMessage;
import com.pelosa.rasutoda.domain.MessageType;

import com.pelosa.rasutoda.dto.*;

import com.pelosa.rasutoda.repository.OttRepository;
import com.pelosa.rasutoda.repository.PartyMemberRepository;
import com.pelosa.rasutoda.repository.UserRepository;
import com.pelosa.rasutoda.repository.PartyRepository;
import com.pelosa.rasutoda.repository.ChatMessageRepository;
import com.pelosa.rasutoda.repository.PaymentRepository;

import com.pelosa.rasutoda.util.AES256Util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.Map;
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
    private final ChatMessageRepository chatMessageRepository;
    private final PaymentRepository paymentRepository;
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

    public List<PartyDto> findJoinedPartiesByUser(User user) {
        return partyMemberRepository.findByMember(user).stream()
                .map(PartyMember::getParty)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<PartyDto> findPartyDetailById(Long partyId) {
        return partyRepository.findById(partyId)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatMessagesByPartyId(Long partyId) {
        return chatMessageRepository.findByParty_IdOrderByCreatedAtAsc(partyId)
                .stream()
                .map(ChatMessageDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatMessageDto saveChatMessage(Long partyId, ChatMessageDto messageDto) {
        User sender = userRepository.findByLoginId(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new IllegalArgumentException("보낸 유저를 찾을 수 없습니다."));



        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("Party not found with ID: " + partyId));

        ChatMessage chatMessage = ChatMessage.builder()
                .party(party)
                .sender(sender)
                .message(messageDto.getMessage())
                .messageType(MessageType.TEXT)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        return ChatMessageDto.fromEntity(savedMessage);
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
                        dto.setOttAccountPassword(aes256Util.decrypt(party.getOttAccountPassword()));
                    } catch (Exception e) {
                        System.err.println("OTT 계정 비밀번호 복호화 실패: " + e.getMessage());
                        dto.setOttAccountPassword("복호화 오류");
                    }
                    LocalDate nextPaymentDate = party.getEndDate() != null ? party.getEndDate().plusMonths(1) : LocalDate.now().plusMonths(1);
                    dto.setNextPaymentDate(nextPaymentDate);

                    dto.setIsLeader(party.getCreator().getId().equals(currentUser.getId()));

                    List<PartyMemberDto> memberDtos = party.getMembers().stream()
                            .map(pm -> PartyMemberDto.builder()
                                    .id(pm.getId())
                                    .nickname(pm.getMember().getNickname())
                                    .role(pm.getRole().name())
                                    .isCurrentUser(pm.getMember().getId().equals(currentUser.getId()))
                                    .build()
                            )
                            .collect(Collectors.toList());
                    dto.setPartyMembers(memberDtos);

                    return dto;
                });
    }

    @Transactional
    public void createParty(PartyCreateForm form){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User creator;
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            String providerId = oauthToken.getName();
            String provider = oauthToken.getAuthorizedClientRegistrationId();

            creator = userRepository.findByProviderAndProviderId(provider, providerId)
                    .orElseThrow(() -> new IllegalArgumentException("OAuth2 사용자(creator) 정보를 찾을 수 없습니다: " + providerId));
        } else {
            String currentUsername = authentication.getName();
            creator = userRepository.findByLoginId(currentUsername)
                    .orElseThrow(() -> new IllegalArgumentException("현재 로그인된 사용자(creator)를 찾을 수 없습니다: " + currentUsername));
        }
        Ott ott = ottRepository.findByNameIgnoreCase(form.getOttName())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 OTT 서비스입니다: " + form.getOttName()));

        String encryptedOttAccountPassword;
        try {
            encryptedOttAccountPassword = aes256Util.encrypt(form.getOttAccountPassword());
        } catch (Exception e) {
            throw new IllegalStateException("OTT 계정 비밀번호 암호화에 실패했습니다.", e);
        }


        Party party = Party.builder()
                .creator(creator)
                .ott(ott)
                .title(form.getPartyTitle())
                .maxMembers(form.getMaxMembers())
                .currentMembers(1)
                .monthlyPrice(form.getMonthlyPrice())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(form.getRemainingDays()))
                .ottAccountId(form.getOttAccountId())
                .ottAccountPassword(encryptedOttAccountPassword)
                .build();

        Party savedParty = partyRepository.save(party);
        System.out.println("Party saved with ID: " + savedParty.getId());

        PartyMember initialMember = PartyMember.builder()
                .party(savedParty)
                .member(creator)
                .joinDate(LocalDate.now())
                .role(PartyMemberRole.LEADER)
                .build();

        partyMemberRepository.save(initialMember);
        savedParty.getMembers().add(initialMember);
    }


    @Transactional
    public void disbandParty(Long partyId, User currentUser) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("해체하려는 파티를 찾을 수 없습니다. ID: " + partyId));
        if (!party.getCreator().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("파티장만 파티를 해체할 수 있습니다.");
        }
        if (party.getMembers().size() > 1) {
            throw new IllegalStateException("파티에 참여 중인 다른 파티원이 있어 파티를 해체할 수 없습니다. 모든 파티원이 탈퇴해야 해체 가능합니다.");
        }
        chatMessageRepository.deleteByPartyId(party.getId());
        paymentRepository.deleteByPartyId(party.getId());
        partyRepository.delete(party);
        System.out.println("파티 " + party.getTitle() + " (ID: " + party.getId() + ")가 성공적으로 해체되었습니다.");
    }


    @Transactional
    public void leaveParty(Long partyId, User currentUser) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("탈퇴하려는 파티를 찾을 수 없습니다. ID: " + partyId));
        PartyMember partyMemberToRemove = partyMemberRepository.findByPartyAndMember(party, currentUser)
                .orElseThrow(() -> new IllegalStateException("해당 파티에 참여하고 있지 않습니다."));

        if (partyMemberToRemove.getRole() == PartyMemberRole.LEADER) {
            throw new IllegalStateException("파티장은 파티를 탈퇴할 수 없습니다. 파티 해체 기능을 이용해주세요.");
        }

        chatMessageRepository.deleteByPartyIdAndSenderId(partyId, currentUser.getId());

        partyMemberRepository.delete(partyMemberToRemove);

        party.setCurrentMembers(party.getCurrentMembers() - 1);

        System.out.println("사용자 " + currentUser.getLoginId() + "가 파티 " + party.getTitle() + "에서 성공적으로 탈퇴했습니다.");
    }


    @Transactional
    public void processPartyJoinPaymentSuccess(Long partyId, User currentUser, String paymentKey, Long amount) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("결제된 파티를 찾을 수 없습니다. ID: " + partyId));

        boolean alreadyJoined = party.getMembers().stream()
                .anyMatch(pm -> pm.getMember().getId().equals(currentUser.getId()));
        if (alreadyJoined) {
            throw new IllegalStateException("이미 파티에 참여 중인 사용자입니다.");
        }

        if (party.getCurrentMembers() >= party.getMaxMembers()) {
            throw new IllegalStateException("파티 정원이 가득 찼습니다.");
        }

        PartyMember newMember = PartyMember.builder()
                .party(party)
                .member(currentUser)
                .joinDate(LocalDate.now())
                .role(PartyMemberRole.MEMBER)
                .build();

        partyMemberRepository.save(newMember);

        party.setCurrentMembers(party.getCurrentMembers() + 1); // @Transactional 덕분에 변경 감지되어 자동 저장

        System.out.println("사용자 " + currentUser.getLoginId() + "가 파티 " + party.getTitle() + "에 성공적으로 참여했습니다.");
    }
    // ====================================================================

    @Transactional
    public void updatePartyAccountInfo(PartyUpdateAccountInfoRequestDto accountInfoRequestDto, Long partyId) throws IllegalAccessException { // <-- 여기가 중요!
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User currentUser = userRepository.findByLoginId(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다."));

        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 파티를 찾을 수 없습니다."));

        if (!party.getCreator().equals(currentUser)) {
            throw new IllegalAccessException("파티 정보를 수정할 권한이 없습니다. (파티 생성자만 수정 가능)");
        }

        party.setOttAccountId(accountInfoRequestDto.getOttAccountId());

        String encryptedOttAccountPassword;
        try {
            encryptedOttAccountPassword = aes256Util.encrypt(accountInfoRequestDto.getOttAccountPassword());
        } catch (Exception e) {
            throw new IllegalStateException("OTT 계정 비밀번호 암호화에 실패했습니다.", e);
        }

        party.setOttAccountPassword(encryptedOttAccountPassword);
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