package com.tenten.studybadge.study.channel.dto;

import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class StudyChannelListResponse {

    private int totalPage;
    private long totalCount;
    private int pageNumber;
    private int pageSize;
    private List<StudyChannelResponse> studyChannels;

    public static StudyChannelListResponse from(Page<StudyChannel> channels, Map<Long, StudyMember> leaderMap) {
        return StudyChannelListResponse.builder()
                .totalPage(channels.getTotalPages())
                .totalCount(channels.getTotalElements())
                .pageNumber(channels.getNumber() + 1)
                .pageSize(channels.getSize())
                .studyChannels(channels.stream()
                        .map((studyChannel -> StudyChannelResponse.from(studyChannel, leaderMap.get(studyChannel.getId()))))
                        .toList())
                .build();
    }

}
