package com.tenten.studybadge.payment.domain.repository;

import com.tenten.studybadge.payment.domain.entity.Payment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByPaymentKeyAndCustomerId(String paymentKey, Long memberId);

    Optional<List<Payment>> findByCustomerId(Long memberId, PageRequest pageRequest);
}
