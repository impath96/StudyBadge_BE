package com.tenten.studybadge.study.channel.domain.entity;

import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.type.study.channel.Category;
import com.tenten.studybadge.type.study.channel.MeetingType;
import com.tenten.studybadge.type.study.channel.RecruitmentStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class StudyChannelTest {

    @DisplayName("특정 회원이 해당 스터디 채널의 리더일 경우 True를 반환한다.")
    @Test
    void isLeader_returnTrue() {

        Member member1 = Member.builder().id(1L).name("회원 1").build();
        Member member2 = Member.builder().id(2L).name("회원 2").build();

        LocalDate now = LocalDate.now();
        StudyChannel studyChannel = StudyChannel.builder()
                .id(1L)
                .name("스터디명")
                .description("스터디 설명")
                .studyDuration(StudyDuration.builder()
                        .studyStartDate(now.plusDays(2))
                        .studyEndDate(now.plusMonths(4))
                        .build())
                .recruitment(Recruitment.builder()
                        .recruitmentNumber(6)
                        .recruitmentStatus(RecruitmentStatus.RECRUITING)
                        .build())
                .category(Category.IT)
                .region("")
                .meetingType(MeetingType.ONLINE)
                .chattingUrl("오픈채팅방 URL")
                .deposit(10_000)
                .viewCnt(4)
                .build();

        StudyMember leader = StudyMember.leader(member1, studyChannel);
        StudyMember member = StudyMember.member(member2, studyChannel);
        studyChannel.getStudyMembers().add(leader);
        studyChannel.getStudyMembers().add(member);

        boolean actual = studyChannel.isLeader(member1);

        Assertions.assertThat(actual).isTrue();

    }

}