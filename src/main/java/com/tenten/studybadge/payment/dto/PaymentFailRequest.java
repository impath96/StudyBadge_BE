package com.tenten.studybadge.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailRequest {

    private String code;

    private String message;

    private String orderId;
}
