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
import com.tenten.studybadge.type.payment.PayType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentConfig paymentConfig;

    @InjectMocks
    private PaymentService paymentService;

    private Member member;
    private PaymentRequest paymentRequest;
    private Payment payment;
    private Payment savedPayment;

    @BeforeEach
    void setUp() {
        member = Member.builder().id(1L).email("test@example.com").build();

        paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(20000);
        paymentRequest.setOrderName("포인트충전");
        paymentRequest.setPayType(PayType.CARD);

        payment = Payment.builder()
                .payType(paymentRequest.getPayType())
                .customer(member)
                .orderName(paymentRequest.getOrderName())
                .build();


        savedPayment = Payment.builder()
                .payType(paymentRequest.getPayType())
                .customer(member)
                .orderName(paymentRequest.getOrderName())
                .build();
    }

    @DisplayName("[결제 요청 성공]")
    @Test
    void requestPayment_Success() {
        // Given
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(paymentConfig.getSuccessUrl()).thenReturn("http://success.url");
        when(paymentConfig.getFailUrl()).thenReturn("http://fail.url");

        // When
        PaymentResponse response = paymentService.requestPayment(1L, paymentRequest);

        // Then
        assertNotNull(response);
        assertEquals("http://success.url", response.getSuccessUrl());
        assertEquals("http://fail.url", response.getFailUrl());
        assertEquals("카드", response.getPayType());
        assertEquals("포인트충전", response.getOrderName());
        assertEquals("test@example.com", response.getCustomerEmail());
    }

    @DisplayName("[존재하지 않는 회원]")
    @Test
    void requestPayment_MemberNotFound() {
        // Given
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundMemberException.class, () -> {
            paymentService.requestPayment(1L, paymentRequest);
        });
    }

    @DisplayName("[조건에 맞지않는 충전금액]")
    @Test
    void requestPayment_InvalidAmount() {
        // Given
        paymentRequest.setAmount(5000);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));

        // When & Then
        assertThrows(InvalidAmountException.class, () -> {
            paymentService.requestPayment(1L, paymentRequest);
        });
    }
}