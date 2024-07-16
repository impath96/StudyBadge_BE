package com.tenten.studybadge.payment.domain.repository;

import com.tenten.studybadge.payment.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
