package com.tenten.studybadge.study.channel.dto;

import com.tenten.studybadge.type.study.channel.Category;
import com.tenten.studybadge.type.study.channel.MeetingType;
import com.tenten.studybadge.type.study.channel.RecruitmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchCondition {

    private MeetingType type;
    private RecruitmentStatus status;
    private Category category;

}
