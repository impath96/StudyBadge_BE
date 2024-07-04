package com.tenten.studybadge.study.channel.domain.entity;

import com.tenten.studybadge.common.BaseEntity;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.type.study.channel.Category;
import com.tenten.studybadge.type.study.channel.MeetingType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StudyChannel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_channel_id")
    private Long id;
    private String name;
    private String description;

    @Embedded
    private Recruitment recruitment;

    @Embedded
    private StudyDuration studyDuration;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private MeetingType meetingType;

    private String region;
    private String chattingUrl;
    private Integer viewCnt;
    private Integer deposit;

    @Builder.Default
    @OneToMany(mappedBy = "studyChannel", cascade = { CascadeType.PERSIST, CascadeType.REMOVE }, orphanRemoval = true)
    private List<StudyMember> members = new ArrayList<>();

    public boolean isStartDateBeforeTo(LocalDate date) {
        return studyDuration.isStartDateBeforeTo(date);
    }

    public boolean isStudyMember(Long memberId) {
        return members.stream().anyMatch(member -> member.getId().equals(memberId));
    }

    public boolean isRecruitmentCompleted() {
        return recruitment.isCompleted();
    }

}
