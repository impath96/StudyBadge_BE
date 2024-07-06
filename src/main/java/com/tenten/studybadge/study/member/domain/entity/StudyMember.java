package com.tenten.studybadge.study.member.domain.entity;

import com.tenten.studybadge.common.BaseEntity;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.type.study.member.StudyMemberRole;
import jakarta.persistence.*;
import lombok.*;

import static com.tenten.studybadge.type.study.member.StudyMemberRole.LEADER;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StudyMember extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_member_id")
    private Long id;

    private Long memberId;

    @Enumerated(EnumType.STRING)
    private StudyMemberRole studyMemberRole;

    private Integer balance;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "study_channel_id")
    private StudyChannel studyChannel;

    public static StudyMember leader(Long memberId, StudyChannel studyChannel) {
        return StudyMember.builder()
                .memberId(memberId)
                .studyChannel(studyChannel)
                .balance(0)
                .studyMemberRole(LEADER)
                .build();
    }

}
