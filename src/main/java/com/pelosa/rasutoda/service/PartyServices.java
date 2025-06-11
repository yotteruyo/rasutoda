package com.pelosa.rasutoda.service;

import com.pelosa.rasutoda.domain.Party;
import com.pelosa.rasutoda.domain.Ott;
import com.pelosa.rasutoda.domain.PartyMember;
import com.pelosa.rasutoda.domain.PartyMemberRole;
import com.pelosa.rasutoda.domain.User;
import com.pelosa.rasutoda.domain.ChatMessage;
import com.pelosa.rasutoda.domain.MessageType;

import com.pelosa.rasutoda.dto.ChatMessageDto;
import com.pelosa.rasutoda.dto.PartyDto;
import com.pelosa.rasutoda.dto.PartyCreateForm;
import com.pelosa.rasutoda.dto.PartyMemberDto; // PartyMemberDto 임포트

import com.pelosa.rasutoda.repository.OttRepository;
import com.pelosa.rasutoda.repository.PartyMemberRepository;
import com.pelosa.rasutoda.repository.UserRepository;
import com.pelosa.rasutoda.repository.PartyRepository;
import com.pelosa.rasutoda.repository.ChatMessageRepository; // ChatMessageRepository 임포트

import com.pelosa.rasutoda.util.AES256Util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication; // 현재 로그인 유저 정보 가져오기 위함
import org.springframework.security.core.context.SecurityContextHolder; // 현재 로그인 유저 정보 가져오기 위함
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList; // 필요시 임포트
import java.util.List;
import java.util.Optional;
import java.util.Map; // Map 임포트 ( PartyServices에는 Map 사용 로직은 없지만, 혹시 다른 곳에서 사용될까봐)
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 클래스 레벨의 기본 트랜잭션 설정
public class PartyServices {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final PartyRepository partyRepository;
    private final OttRepository ottRepository;
    private final PartyMemberRepository partyMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AES256Util aes256Util;

    public List<PartyDto> findAllParties() {
        return partyRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PartyDto> findPartiesByOtt(String ottName) {
        // PartyRepository에 findByOttNameIgnoreCase(String ottName) 메서드 필요
        return partyRepository.findByOttNameIgnoreCase(ottName).stream() // <-- 수정됨
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PartyDto> findCreatedPartiesByCreator(User creator) {
        return partyRepository.findByCreator(creator).stream() // PartyRepository에 findByCreator(User creator) 필요
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PartyDto> findJoinedPartiesByUser(User user) {
        return partyMemberRepository.findByMember(user).stream() // PartyMemberRepository에 findByMember(User member) 필요
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
        return chatMessageRepository.findByParty_IdOrderByCreatedAtAsc(partyId) // ChatMessageRepository에 findByParty_IdOrderByCreatedAtAsc 필요
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
                    // 1. 현재 유저가 이 파티의 멤버인지 확인 (접근 권한)
                    boolean isMemberOfThisParty = party.getMembers().stream()
                            .anyMatch(pm -> pm.getMember().getId().equals(currentUser.getId()));
                    if (!isMemberOfThisParty) {
                        return null; // 또는 throw new IllegalAccessException("접근 권한이 없습니다.");
                    }

                    // 2. 기본 PartyDto 변환 (기존 convertToDto 재활용)
                    PartyDto dto = convertToDto(party);

                    // 3. myparty.html에 필요한 추가 정보 설정
                    //    - OTT 계정 정보 (복호화)
                    try {
                        dto.setOttAccountId(party.getOttAccountId());
                        dto.setOttAccountPassword(aes256Util.decrypt(party.getOttAccountPassword())); // 복호화된 평문 비밀번호 설정
                    } catch (Exception e) {
                        System.err.println("OTT 계정 비밀번호 복호화 실패: " + e.getMessage());
                        dto.setOttAccountPassword("복호화 오류"); // 또는 null
                    }
                    //    - 다음 결제일 (예시: endDate 기준으로 월 단위로 계산)
                    LocalDate nextPaymentDate = party.getEndDate() != null ? party.getEndDate().plusMonths(1) : LocalDate.now().plusMonths(1);
                    dto.setNextPaymentDate(nextPaymentDate);

                    //    - 현재 유저가 파티장인지 여부
                    dto.setIsLeader(party.getCreator().getId().equals(currentUser.getId()));

                    //    - 파티 멤버 목록 (PartyMemberDto로 변환)
                    List<PartyMemberDto> memberDtos = party.getMembers().stream()
                            .map(pm -> PartyMemberDto.builder() // PartyMemberDto 빌더 사용
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
        String currentUsername = authentication.getName(); // CustomUserDetailsService에서 설정한 loginId
        User creator = userRepository.findByLoginId(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("현재 로그인된 사용자를 찾을 수 없습니다."));
        Ott ott = ottRepository.findByNameIgnoreCase(form.getOttName()) // PartyRepository에 findByNameIgnoreCase 필요
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
//                .content("새로운 " + form.getOttName() + " 파티입니다.") // content 필드에 값 할당 (Party 엔티티에 content 필드 필요)
                .build();

        Party savedParty = partyRepository.save(party);
        System.out.println("Party saved with ID: " + savedParty.getId());

        PartyMember initialMember = PartyMember.builder()
                .party(savedParty)
                .member(creator)
                .joinDate(LocalDate.now())
                .role(PartyMemberRole.LEADER) // 파티장 역할 부여
                .build();

        partyMemberRepository.save(initialMember);
        savedParty.getMembers().add(initialMember); // 양방향 관계 일관성 유지
    }

    // ====================================================================
    // processPartyJoinPaymentSuccess 메서드 (중괄호 문제 해결)
    // ====================================================================
    @Transactional // DB 변경이 있으므로 @Transactional
    public void processPartyJoinPaymentSuccess(Long partyId, User currentUser, String paymentKey, Long amount) {
        // 1. 파티 정보 조회
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("결제된 파티를 찾을 수 없습니다. ID: " + partyId));

        // 2. 현재 유저가 이미 파티 멤버인지 확인 (중복 가입 방지)
        boolean alreadyJoined = party.getMembers().stream()
                .anyMatch(pm -> pm.getMember().getId().equals(currentUser.getId()));
        if (alreadyJoined) {
            throw new IllegalStateException("이미 파티에 참여 중인 사용자입니다.");
        }

        // 3. 파티 멤버 수 확인 (정원 초과 방지)
        // 이 if문은 이미 참여한 사용자가 아니면 실행되어야 합니다.
        if (party.getCurrentMembers() >= party.getMaxMembers()) {
            throw new IllegalStateException("파티 정원이 가득 찼습니다.");
        }

        // 4. 새로운 PartyMember 생성 (역할: MEMBER)
        PartyMember newMember = PartyMember.builder()
                .party(party)
                .member(currentUser)
                .joinDate(LocalDate.now())
                .role(PartyMemberRole.MEMBER) // 일반 멤버 역할 부여
                .build();

        // 5. PartyMember 저장
        partyMemberRepository.save(newMember);

        // 6. Party의 현재 멤버 수 업데이트
        party.setCurrentMembers(party.getCurrentMembers() + 1); // @Transactional 덕분에 변경 감지되어 자동 저장

        // TODO: (선택 사항) 결제 정보 저장 (PaymentsService에서 이미 수행되므로 여기서는 호출하지 않습니다.)

        System.out.println("사용자 " + currentUser.getLoginId() + "가 파티 " + party.getTitle() + "에 성공적으로 참여했습니다.");
    }
    // ====================================================================


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