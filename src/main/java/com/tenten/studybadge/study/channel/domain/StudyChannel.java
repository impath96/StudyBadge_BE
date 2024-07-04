package com.tenten.studybadge.study.channel.domain;

import com.tenten.studybadge.common.BaseEntity;
import com.tenten.studybadge.type.study.channel.Category;
import com.tenten.studybadge.type.study.channel.MeetingType;
import jakarta.persistence.*;
import lombok.*;

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

}
