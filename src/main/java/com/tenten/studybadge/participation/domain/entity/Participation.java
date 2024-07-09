package com.tenten.studybadge.participation.domain.entity;

import com.tenten.studybadge.common.BaseEntity;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.type.participation.ParticipationStatus;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Participation extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_channel_participation_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "study_channel_id")
    private StudyChannel studyChannel;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private ParticipationStatus participationStatus;

    public static Participation create(Member member, StudyChannel studyChannel) {
        return Participation.builder()
                .member(member)
                .studyChannel(studyChannel)
                .participationStatus(ParticipationStatus.APPROVE_WAITING)
                .build();
    }

    public boolean isCreatedBy(Member member) {
        return member.getId().equals(this.member.getId());
    }

    public void cancel() {
        this.participationStatus = ParticipationStatus.CANCELED;
    }

    public void approve() {
        this.participationStatus = ParticipationStatus.APPROVED;
    }

}
