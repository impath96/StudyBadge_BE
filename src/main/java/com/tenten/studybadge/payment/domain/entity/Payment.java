package com.tenten.studybadge.payment.domain.entity;

import com.tenten.studybadge.common.BaseEntity;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.type.payment.PayType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PayType payType;

    private Long amount;

    private String orderName;

    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "customer_id")
    private Member customer;

    private String paymentKey;

    private String failReason;

    private boolean successYN;

    private boolean cancelYN;

    private String cancelReason;
}