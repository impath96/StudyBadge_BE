package com.tenten.studybadge.payment.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PaymentConfirm {

    private String paymentKey;

    private String orderId;

    private String orderName;

    private String method;

    private int totalAmount;

    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;
}
