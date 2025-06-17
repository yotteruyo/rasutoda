package com.pelosa.rasutoda.repository;

import com.pelosa.rasutoda.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
        @Modifying
        @Query("DELETE FROM Payment p WHERE p.party.id = :partyId")
        void deleteByPartyId(@Param("partyId") Long partyId);
}