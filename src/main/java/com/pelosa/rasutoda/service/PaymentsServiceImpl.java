package com.pelosa.rasutoda.service;

import com.pelosa.rasutoda.domain.Payment; // Payment 엔티티 임포트
import com.pelosa.rasutoda.domain.Party; // Party 엔티티 임포트
import com.pelosa.rasutoda.domain.User; // User 엔티티 임포트
import com.pelosa.rasutoda.repository.PaymentRepository; // PaymentRepository 주입
import com.pelosa.rasutoda.repository.PartyRepository;     // Party 엔티티 조회를 위해 주입
import com.pelosa.rasutoda.repository.UserRepository;     // User 엔티티 조회를 위해 주입 (선택 사항)
import com.pelosa.rasutoda.service.PartyServices;          // PartyServices 임포트 (파티 참여 로직 위임)
import com.pelosa.rasutoda.service.PaymentsService; // PaymentsService 인터페이스 임포트

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // @Value 임포트
import org.springframework.http.HttpHeaders; // HttpHeaders 임포트
import org.springframework.http.HttpEntity; // HttpEntity 임포트
import org.springframework.http.HttpStatus; // HttpStatus 임포트
import org.springframework.http.ResponseEntity; // ResponseEntity 임포트
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate; // RestTemplate 임포트

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service // Spring Service 빈으로 등록
@RequiredArgsConstructor // final 필드 자동 주입
@Transactional // DB 변경이 포함되므로 트랜잭션 처리
public class PaymentsServiceImpl implements PaymentsService { // PaymentsService 인터페이스 구현

    private final PaymentRepository paymentRepository;
    private final PartyRepository partyRepository;
    private final PartyServices partyService; // PartyServices를 통한 파티 멤버 추가 등 위임
    private final UserRepository userRepository; // User 엔티티 조회를 위해 주입 (PartyServices에서 User 받아오지만, 필요시 직접 사용)

    // application.properties에서 토스 시크릿 키 주입 (제공하지 않음)
    // 이 값은 application.properties에 직접 넣어주셔야 합니다.
    @Value("${toss.secret.key}")
    private String tossSecretKey;

    @Override
    public void processPaymentSuccess(String paymentType, String orderId, String paymentKey, Long amount, User currentUser) {
        try {
            // 1. 토스페이먼츠 승인 API 호출 (서버 대 서버 통신)
            // RestTemplate 빈으로 등록하여 사용하면 더 좋습니다. (config에서 @Bean으로)
            RestTemplate restTemplate = new RestTemplate();

            // Authorization 헤더 생성 (Basic 인증)
            // tossSecretKey는 "sk_test_..." 형태이며, 뒤에 콜론(:)을 붙여 Base64 인코딩합니다.
            String encodedAuth = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedAuth);
            headers.set("Content-Type", "application/json");

            Map<String, Object> requestBody = Map.of(
                    "orderId", orderId,
                    "amount", amount,
                    "paymentKey", paymentKey
            );

            URI uri = new URI("https://api.tosspayments.com/v1/payments/confirm");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> tossResponse = restTemplate.postForEntity(uri, entity, Map.class);

            if (tossResponse.getStatusCode() != HttpStatus.OK) {
                // 토스 API에서 결제 승인 실패 응답이 왔을 경우
                Map<String, String> errorBody = tossResponse.getBody();
                String errorMessage = errorBody.getOrDefault("message", "토스 결제 승인 실패");
                String errorCode = errorBody.getOrDefault("code", "TOSS_UNKNOWN_ERROR");
                throw new IllegalArgumentException("결제 승인 실패: " + errorMessage + " (코드: " + errorCode + ")");
            }

            // 2. 결제 승인 성공 시 후속 로직 (파티 참여, 결제 내역 저장 등)
            Long partyId = extractPartyIdFromOrderIdInternal(orderId); // orderId에서 파티 ID 추출
            Party party = partyRepository.findById(partyId)
                    .orElseThrow(() -> new IllegalArgumentException("결제된 파티를 찾을 수 없습니다. ID: " + partyId));

            // 결제 내역 저장 (Payment 엔티티 사용)
            Payment payment = Payment.builder()
                    .buyer(currentUser)
                    .party(party)
                    .orderId(orderId)
                    .paymentKey(paymentKey)
                    .amount(amount)
                    .paymentType(paymentType)
                    .build();
            paymentRepository.save(payment);

            // PartyServices를 통해 파티 가입 로직 수행 (멤버 추가 등)
            partyService.processPartyJoinPaymentSuccess(partyId, currentUser, paymentKey, amount);

            System.out.println("PaymentsService: 결제 승인 및 파티 가입 로직 처리 완료 for orderId: " + orderId);

        } catch (Exception e) {
            System.err.println("결제 성공 처리 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            // 실패 시 RuntimeException으로 다시 던져서 @Transactional이 롤백되도록
            throw new RuntimeException("결제 처리 중 시스템 오류가 발생했습니다.", e);
        }
    }

    // PartyId 추출 로직 (PaymentsServiceImpl 내부 메서드로)
    private Long extractPartyIdFromOrderIdInternal(String orderId) {
        // 예상하는 orderId 형식: "ORDER_PARTYID_UUID" (예: "ORDER_1_3e7853d8375a469fa44d0e8efbd47ed5")
        // 또는 "ORDER_PARTYID" (만약 UUID가 없을 경우 - 드물겠지만 안전성 위해 고려)

        String[] parts = orderId.split("_");

        // orderId가 "ORDER_숫자_UUID" 형태인지 확인하고 파티 ID 추출
        // parts.length가 3 이상이고, 첫 부분이 "ORDER"인지 확인
        if (parts.length >= 2 && parts[0].equals("ORDER")) {
            try {
                // 파티 ID는 두 번째 요소 (인덱스 1)에 있습니다.
                return Long.parseLong(parts[1]);
            } catch (NumberFormatException e) {
                // parts[1]이 숫자가 아닐 경우 (예: "ORDER_UUID_PARTY")
                throw new IllegalArgumentException("Order ID에서 파티 ID 추출 실패: 파티 ID 부분이 숫자가 아닙니다. orderId: " + orderId, e);
            }
        }

        // 위의 조건에 맞지 않는 경우 (예상치 못한 형식)
        throw new IllegalArgumentException("Order ID에서 파티 ID를 추출할 수 없습니다: orderId 형식이 잘못되었습니다. orderId: " + orderId);
    }
}