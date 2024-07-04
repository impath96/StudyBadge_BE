package com.tenten.studybadge.participation.domain.entity;

import com.tenten.studybadge.common.BaseEntity;
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

    private Long memberId;

    @Enumerated(EnumType.STRING)
    private ParticipationStatus participationStatus;

    public static Participation create(Long memberId, StudyChannel studyChannel) {
        return Participation.builder()
                .memberId(memberId)
                .studyChannel(studyChannel)
                .participationStatus(ParticipationStatus.APPROVE_WAITING)
                .build();
    }

}
