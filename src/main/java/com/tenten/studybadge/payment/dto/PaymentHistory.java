package com.tenten.studybadge.payment.dto;

import com.tenten.studybadge.payment.domain.entity.Payment;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentHistory {

    private LocalDateTime createdAt;

    private Long amount;


    public static List<PaymentHistory> listToResponse(List<Payment> payment) {

        return payment.stream().map(PaymentHistory::toResponse).collect(Collectors.toList());
    }

    public static PaymentHistory toResponse(Payment payment) {

        return PaymentHistory.builder()
                .amount(payment.getAmount())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}