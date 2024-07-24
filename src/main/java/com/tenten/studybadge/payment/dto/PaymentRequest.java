package com.tenten.studybadge.payment.dto;

import com.tenten.studybadge.payment.domain.entity.Payment;
import com.tenten.studybadge.type.payment.PayType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentRequest {

    @NotNull
    private PayType payType;

    @NotNull
    private Integer amount;

    @NotBlank
    private String orderName;

    public Payment toEntity() {

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString();
        String orderId = date + "_" + uuid;

        return Payment.builder()
                .payType(payType)
                .amount(amount)
                .orderName(orderName)
                .orderId(orderId)
                .successYN(false)
                .build();
    }
}
