package com.tenten.studybadge.payment.dto;

import lombok.*;


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

    private String requestedAt;

    private String approvedAt;
}
