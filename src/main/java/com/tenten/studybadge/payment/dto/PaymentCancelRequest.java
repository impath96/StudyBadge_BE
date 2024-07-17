package com.tenten.studybadge.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCancelRequest {

    @NotBlank
    private String paymentKey;

    @NotBlank
    private String cancelReason;
}
