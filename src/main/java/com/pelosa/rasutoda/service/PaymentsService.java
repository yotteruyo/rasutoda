// src/main/java/com.pelosa.rasutoda.service/PaymentsService.java
package com.pelosa.rasutoda.service;

import com.pelosa.rasutoda.domain.User; // User 임포트

public interface PaymentsService {
    /**
     * 토스페이먼츠 결제 성공 후 비즈니스 로직을 처리합니다.
     * 토스 API 승인 요청, 결제 내역 저장, 파티 참여(멤버 추가) 등을 포함합니다.
     * @param paymentType 결제 수단 (카드, 간편결제 등)
     * @param orderId 토스페이먼츠 주문 ID
     * @param paymentKey 토스페이먼츠 Payment Key
     * @param amount 결제 금액
     * @param currentUser 현재 로그인한 사용자 (결제 주체)
     * @throws IllegalArgumentException 결제 승인 실패, 유효하지 않은 파티 ID 등 비즈니스 로직 오류
     * @throws RuntimeException 시스템 오류 발생 시 (트랜잭션 롤백 유도)
     */
    void processPaymentSuccess(String paymentType, String orderId, String paymentKey, Long amount, User currentUser);
}