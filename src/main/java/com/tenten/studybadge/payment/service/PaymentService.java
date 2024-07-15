package com.tenten.studybadge.payment.service;

import com.tenten.studybadge.common.config.PaymentConfig;
import com.tenten.studybadge.common.exception.member.NotFoundMemberException;
import com.tenten.studybadge.common.exception.payment.InvalidAmountException;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.member.domain.repository.MemberRepository;
import com.tenten.studybadge.payment.domain.entity.Payment;
import com.tenten.studybadge.payment.domain.repository.PaymentRepository;
import com.tenten.studybadge.payment.dto.PaymentRequest;
import com.tenten.studybadge.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentConfig paymentConfig;
    @Transactional
    public PaymentResponse requestPayment(Long memberId, PaymentRequest paymentRequest) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);

        if(paymentRequest.getAmount() < 10000) {
            throw new InvalidAmountException();
        }

        Payment payment = paymentRequest.toEntity();

        Payment updatedPayment = payment.toBuilder()
                .customer(member)
                .build();
        Payment savedPayment = paymentRepository.save(updatedPayment);

        return PaymentResponse.toResponse(savedPayment, paymentConfig.getSuccessUrl(), paymentConfig.getFailUrl());
    }
}
