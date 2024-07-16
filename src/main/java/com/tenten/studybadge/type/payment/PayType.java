package com.tenten.studybadge.type.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum PayType {

    CARD("카드"),
    CASH("현금"),
    POINT("포인트");

    private String description;
}