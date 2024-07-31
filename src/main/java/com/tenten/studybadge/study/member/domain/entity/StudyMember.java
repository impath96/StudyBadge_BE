package com.tenten.studybadge.study.member.domain.entity;

import com.tenten.studybadge.common.BaseEntity;
import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.member.dto.StudyMemberInfoResponse;
import com.tenten.studybadge.type.study.member.StudyMemberRole;
import com.tenten.studybadge.type.study.member.StudyMemberStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import static com.tenten.studybadge.type.study.member.StudyMemberRole.*;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE STUDY_MEMBER SET study_member_status = 'LEAVE' WHERE study_member_id = ?")
public class StudyMember extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_member_id")
    private Long id;

    @Setter
    @Enumerated(EnumType.STRING)
    private StudyMemberRole studyMemberRole;

    @Setter
    @Enumerated(EnumType.STRING)
    private StudyMemberStatus studyMemberStatus;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "study_channel_id")
    private StudyChannel studyChannel;

    public static StudyMember leader(Member member, StudyChannel studyChannel) {
        return StudyMember.builder()
                .member(member)
                .studyChannel(studyChannel)
                .studyMemberRole(LEADER)
                .studyMemberStatus(StudyMemberStatus.PARTICIPATING)
                .build();
    }

    public static StudyMember member(Member member, StudyChannel studyChannel) {
        return StudyMember.builder()
                .member(member)
                .studyChannel(studyChannel)
                .studyMemberRole(STUDY_MEMBER)
                .studyMemberStatus(StudyMemberStatus.PARTICIPATING)
                .build();
    }

    public boolean isLeader() {
        return this.studyMemberRole.equals(LEADER);
    }

    public boolean isSubLeader() {
        return this.studyMemberRole.equals(SUB_LEADER);
    }

    public StudyMemberInfoResponse toResponse() {
        return StudyMemberInfoResponse.builder()
                .studyMemberId(this.id)
                .memberId(this.member.getId())
                .name(this.member.getName())
                .imageUrl(this.member.getImgUrl())
                .badgeLevel(this.member.getBadgeLevel())
                .role(this.studyMemberRole)
                .build();
    }

}
