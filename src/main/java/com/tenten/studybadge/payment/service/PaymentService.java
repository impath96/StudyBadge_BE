package com.tenten.studybadge.payment.service;

import com.tenten.studybadge.common.config.PaymentConfig;
import com.tenten.studybadge.common.exception.member.NotFoundMemberException;
import com.tenten.studybadge.common.exception.payment.InvalidAmountException;
import com.tenten.studybadge.common.exception.payment.NotFoundOrderException;
import com.tenten.studybadge.common.exception.payment.NotMatchAmountException;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.member.domain.repository.MemberRepository;
import com.tenten.studybadge.payment.domain.entity.Payment;
import com.tenten.studybadge.payment.domain.repository.PaymentRepository;
import com.tenten.studybadge.payment.dto.PaymentConfirmRequest;
import com.tenten.studybadge.payment.dto.PaymentRequest;
import com.tenten.studybadge.payment.dto.PaymentResponse;
import com.tenten.studybadge.payment.dto.PaymentConfirm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.tenten.studybadge.common.constant.PaymentConstant.BASIC;
import static com.tenten.studybadge.common.constant.PaymentConstant.COLON;

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
    @Transactional
    public PaymentConfirm paymentConfirm(PaymentConfirmRequest confirmRequest) {

        Payment payment = verifyPayment(confirmRequest.getOrderId(), confirmRequest.getAmount());
        PaymentConfirm result = requestPaymentAccept(confirmRequest);

        Member updatedCustomer = payment.getCustomer().toBuilder()
                .point((int) (payment.getCustomer().getPoint() + confirmRequest.getAmount()))
                .build();
        memberRepository.save(updatedCustomer);

        Payment updatedPayment = payment.toBuilder()
                .successYN(true)
                .paymentKey(confirmRequest.getPaymentKey())
                .build();
        paymentRepository.save(updatedPayment);

        return result;
    }

    public Payment verifyPayment(String orderId, Long amount) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(NotFoundOrderException::new);

        if (!payment.getAmount().equals(amount)) {

            throw new NotMatchAmountException();
        }

        return payment;
    }

    @Transactional
    public PaymentConfirm requestPaymentAccept(PaymentConfirmRequest confirmRequest) {

        WebClient webClient = WebClient.builder()
                .baseUrl(PaymentConfig.TOSS_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, createAuthorizationHeader())
                .build();

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path(confirmRequest.getPaymentKey())
                        .build())
                .bodyValue(confirmRequest)
                .retrieve()
                .bodyToMono(PaymentConfirm.class)
                .block();
    }

    private String createAuthorizationHeader() {

        String auth = paymentConfig.getTestSecretKey() + COLON;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));

        return BASIC + new String(encodedAuth);
    }
}
