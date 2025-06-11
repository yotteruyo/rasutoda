package com.pelosa.rasutoda.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime; // LocalDateTime 임포트

@Entity
@Table(name = "payments") // 테이블명
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // @CreatedDate 사용 시 필요
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 결제 ID (DB 자동 생성)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User buyer; // 구매자 (파티에 참여하는 유저)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party; // 결제된 파티

    @Column(nullable = false, unique = true, length = 50)
    private String orderId; // 토스페이먼츠 주문 ID (고유해야 함)

    @Column(nullable = false, unique = true, length = 50)
    private String paymentKey; // 토스페이먼츠 Payment Key

    @Column(nullable = false)
    private Long amount; // 결제 금액

    @Column(nullable = false)
    private String paymentType; // 결제 수단 (예: 카드, 간편결제)

    @CreatedDate // 결제 일시 자동 기록
    @Column(nullable = false, updatable = false)
    private LocalDateTime paidAt; // 결제 완료 일시

    @Builder
    public Payment(User buyer, Party party, String orderId, String paymentKey, Long amount, String paymentType) {
        this.buyer = buyer;
        this.party = party;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.paymentType = paymentType;

    }
}