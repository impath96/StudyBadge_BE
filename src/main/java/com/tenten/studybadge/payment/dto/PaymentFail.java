package com.tenten.studybadge.payment.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentFail {

    private String errorCode;

    private String errorMessage;

    private String orderId;
}
