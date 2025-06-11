// src/main/java/com.pelosa.rasutoda.controller.api/PaymentController.java
package com.pelosa.rasutoda.controller.api;

import com.pelosa.rasutoda.service.PaymentsService;
import com.pelosa.rasutoda.service.UserService;
import com.pelosa.rasutoda.domain.User;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentsService paymentsService;
    private final UserService userService;


    @PostMapping("/confirm")
    public ResponseEntity<Map<String, String>> confirmPaymentApi(
            @RequestBody Map<String, Object> tossRequest
    ) {
        String paymentType = (String) tossRequest.get("paymentType");
        String orderId = (String) tossRequest.get("orderId");
        String paymentKey = (String) tossRequest.get("paymentKey");
        Long amount = ((Number) tossRequest.get("amount")).longValue();

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인 상태가 아닙니다."));
            }
            String loginId = authentication.getName();
            User currentUser = userService.findUserByLoginId(loginId)
                    .orElseThrow(() -> new IllegalStateException("로그인된 사용자를 찾을 수 없습니다: " + loginId));

            paymentsService.processPaymentSuccess(paymentType, orderId, paymentKey, amount, currentUser);

            Long partyId = extractPartyIdFromOrderIdInternal(orderId);
            return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "결제 성공", "redirectUrl", "/mypage/my-party/" + partyId));

        } catch (IllegalArgumentException e) {
            System.err.println("결제 API 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "FAILURE", "message", e.getMessage()));
        } catch (IllegalStateException e) {
            System.err.println("결제 API 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("status", "FAILURE", "message", e.getMessage()));
        } catch (Exception e) {
            System.err.println("결제 API 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", "FAILURE", "message", "서버 오류가 발생했습니다."));
        }
    }


    private Long extractPartyIdFromOrderIdInternal(String orderId) {
        String[] parts = orderId.split("_");
        if (parts.length >= 2 && parts[0].equals("ORDER")) {
            try {
                return Long.parseLong(parts[1]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Order ID에서 파티 ID 추출 실패: 숫자가 아닙니다. orderId: " + orderId, e);
            }
        }
        throw new IllegalArgumentException("Order ID에서 파티 ID를 추출할 수 없습니다: orderId 형식이 잘못되었습니다. orderId: " + orderId);
    }
}