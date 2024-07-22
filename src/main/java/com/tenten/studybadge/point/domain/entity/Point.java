package com.tenten.studybadge.point.domain.entity;

import com.tenten.studybadge.common.BaseEntity;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.payment.domain.entity.Payment;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.type.point.PointHistoryType;
import com.tenten.studybadge.type.point.TransferType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Point extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private Long amount;

    @Enumerated(EnumType.STRING)
    private PointHistoryType historyType;

    @Enumerated(EnumType.STRING)
    private TransferType transferType;
}
