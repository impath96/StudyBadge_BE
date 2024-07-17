package com.tenten.studybadge.payment.service;

import com.tenten.studybadge.common.config.PaymentConfig;
import com.tenten.studybadge.common.exception.member.NotFoundMemberException;
import com.tenten.studybadge.common.exception.payment.InvalidAmountException;
import com.tenten.studybadge.common.exception.payment.NotEnoughPointException;
import com.tenten.studybadge.common.exception.payment.NotFoundOrderException;
import com.tenten.studybadge.common.exception.payment.NotMatchAmountException;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.member.domain.repository.MemberRepository;
import com.tenten.studybadge.payment.domain.entity.Payment;
import com.tenten.studybadge.payment.domain.repository.PaymentRepository;
import com.tenten.studybadge.payment.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

import static com.tenten.studybadge.common.constant.PaymentConstant.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentConfig paymentConfig;

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
    public PaymentConfirm confirmPayment(PaymentConfirmRequest confirmRequest) {

        Payment payment = verifyPayment(confirmRequest.getOrderId(), confirmRequest.getAmount());
        PaymentConfirm result = requestAcceptPayment(confirmRequest);

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
    @Transactional
    public Map<String, Object> cancelPayment(Long memberId, PaymentCancelRequest cancelRequest) {

        Payment payment = paymentRepository
                .findByPaymentKeyAndCustomerId(cancelRequest.getPaymentKey(), memberId)
                .orElseThrow(NotFoundOrderException::new);

        if (payment.getCustomer().getPoint() >= payment.getAmount()) {

            Payment canceldPayment = payment.toBuilder()
                    .cancelYN(true)
                    .cancelReason(cancelRequest.getCancelReason())
                    .build();
            paymentRepository.save(canceldPayment);

            Member updatedMember = payment.getCustomer().toBuilder()
                    .point((int) (payment.getCustomer().getPoint() + payment.getAmount()))
                    .build();
            memberRepository.save(updatedMember);

            return requestCancelPayment(cancelRequest);
        }

        throw new NotEnoughPointException();
    }

    public Map<String, Object> requestCancelPayment(PaymentCancelRequest cancelRequest) {

        WebClient webClient = TossWebClient();

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path(cancelRequest.getPaymentKey() + CANCEL_URL)
                .build())
                .bodyValue(Collections.singletonMap(CANCEL_REASON, cancelRequest.getCancelReason()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }

    public Payment verifyPayment(String orderId, Long amount) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(NotFoundOrderException::new);

        if (!payment.getAmount().equals(amount)) {

            throw new NotMatchAmountException();
        }

        return payment;
    }

    public PaymentConfirm requestAcceptPayment(PaymentConfirmRequest confirmRequest) {

        WebClient webClient = TossWebClient();

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
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        return BASIC + encodedAuth;
    }

    private WebClient TossWebClient() {

        return WebClient.builder()
                .baseUrl(PaymentConfig.TOSS_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, createAuthorizationHeader())
                .build();
    }
}
