package com.pelosa.rasutoda.repository;

import com.pelosa.rasutoda.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
//    Optional<Payment> findByOrderId(String orderId);
}