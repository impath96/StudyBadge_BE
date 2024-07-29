package com.tenten.studybadge.payment.dto;

import com.tenten.studybadge.payment.domain.entity.Payment;
import lombok.*;
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

    private String payType;

    private Integer amount;

    private String orderName;

    private String orderId;

    private String customerEmail;

    private String customerName;

    private String successUrl;

    private String failUrl;

    private String failReason;

    private boolean cancelYN;

    private String cancelReason;


    public static PaymentResponse toResponse(Payment payment, String successUrl, String failUrl) {
        return PaymentResponse.builder()
                .payType(payment.getPayType().getDescription())
                .amount(payment.getAmount())
                .orderName(payment.getOrderName())
                .orderId(payment.getOrderId())
                .cancelYN(payment.isCancelYN())
                .successUrl(successUrl)
                .failUrl(failUrl)
                .customerEmail(payment.getCustomer().getEmail())
                .customerName(payment.getCustomer().getName())
                .failReason(payment.getFailReason())
                .build();
    }
}