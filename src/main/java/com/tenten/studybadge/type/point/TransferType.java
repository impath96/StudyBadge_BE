package com.tenten.studybadge.type.point;

public enum TransferType {

    STUDY_REWARD, // 정산(환급)    PaymentHistory.EARNED (+)
    PAYMENT_CHARGE, // 충전    PaymentHistory.EARNED (+)
    STUDY_DEPOSIT,// 예치금  PaymentHistory.SPENT (-)
    PAYMENT_CANCEL // 취소 PaymentHistory.DEDUCTED (-)
}
