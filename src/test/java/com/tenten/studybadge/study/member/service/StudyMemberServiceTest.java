package com.tenten.studybadge.study.member.service;

import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.member.domain.entity.StudyMember;
import com.tenten.studybadge.study.member.domain.repository.StudyMemberRepository;
import com.tenten.studybadge.study.member.dto.StudyMembersResponse;
import com.tenten.studybadge.type.member.BadgeLevel;
import com.tenten.studybadge.type.study.member.StudyMemberRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StudyMemberServiceTest {

    @InjectMocks
    private StudyMemberService studyMemberService;

    @Mock
    private StudyMemberRepository studyMemberRepository;

    @DisplayName("[스터디 멤버 리스트 조회 테스트]")
    @Nested
    class GetStudyMembersTest {

        Member member1;
        Member member2;

        @BeforeEach
        void setUp() {
            member1 = Member.builder()
                    .id(1L)
                    .name("회원1")
                    .banCnt(2)
                    .imgUrl("imageUrl1")
                    .badgeLevel(BadgeLevel.SILVER)
                    .build();
            member2 = Member.builder()
                    .id(2L)
                    .name("회원2")
                    .banCnt(2)
                    .imgUrl("imageUrl2")
                    .badgeLevel(BadgeLevel.NONE)
                    .build();

        }

        @DisplayName("리더가 스터디 멤버를 조회할 경우 isLeader는 True")
        @Test
        void success_getStudyMembersForLeader() {
            StudyChannel studyChannel = StudyChannel.builder().id(1L).build();

            StudyMember leader = StudyMember.leader(member1, studyChannel);
            StudyMember studyMember = StudyMember.member(member2, studyChannel);
            List<StudyMember> studyMembers = List.of(leader, studyMember);

            given(studyMemberRepository.existsByStudyChannelIdAndMemberId(1L, 1L))
                    .willReturn(true);

            given(studyMemberRepository.findAllByStudyChannelIdWithMember(1L)).willReturn(studyMembers);

            StudyMembersResponse response = studyMemberService.getStudyMembers(1L, 1L);

            assertThat(response.isLeader()).isTrue();
            assertThat(response.getStudyMembers().size()).isEqualTo(2);
            assertThat(response.getStudyMembers().get(0).getMemberId()).isEqualTo(1L);
            assertThat(response.getStudyMembers().get(0).getImageUrl()).isEqualTo("imageUrl1");
            assertThat(response.getStudyMembers().get(0).getName()).isEqualTo("회원1");
            assertThat(response.getStudyMembers().get(0).getRole()).isEqualTo(StudyMemberRole.LEADER);
            assertThat(response.getStudyMembers().get(0).getBadgeLevel()).isEqualTo(BadgeLevel.SILVER);
            assertThat(response.getStudyMembers().get(1).getMemberId()).isEqualTo(2L);
            assertThat(response.getStudyMembers().get(1).getImageUrl()).isEqualTo("imageUrl2");
            assertThat(response.getStudyMembers().get(1).getName()).isEqualTo("회원2");
            assertThat(response.getStudyMembers().get(1).getRole()).isEqualTo(StudyMemberRole.STUDY_MEMBER);
            assertThat(response.getStudyMembers().get(1).getBadgeLevel()).isEqualTo(BadgeLevel.NONE);

        }

        @DisplayName("스터디 멤버가 스터디 멤버를 조회할 경우 isLeader는 False")
        @Test
        void success_getStudyMembersForStudyMember() {
            StudyChannel studyChannel = StudyChannel.builder().id(1L).build();

            StudyMember leader = StudyMember.leader(member1, studyChannel);
            StudyMember studyMember = StudyMember.member(member2, studyChannel);
            List<StudyMember> studyMembers = List.of(leader, studyMember);

            given(studyMemberRepository.existsByStudyChannelIdAndMemberId(1L, 2L))
                    .willReturn(true);

            given(studyMemberRepository.findAllByStudyChannelIdWithMember(1L)).willReturn(studyMembers);

            StudyMembersResponse response = studyMemberService.getStudyMembers(1L, 2L);

            assertThat(response.isLeader()).isFalse();
            assertThat(response.getStudyMembers().size()).isEqualTo(2);
            assertThat(response.getStudyMembers().get(0).getMemberId()).isEqualTo(1L);
            assertThat(response.getStudyMembers().get(0).getImageUrl()).isEqualTo("imageUrl1");
            assertThat(response.getStudyMembers().get(0).getName()).isEqualTo("회원1");
            assertThat(response.getStudyMembers().get(0).getRole()).isEqualTo(StudyMemberRole.LEADER);
            assertThat(response.getStudyMembers().get(0).getBadgeLevel()).isEqualTo(BadgeLevel.SILVER);
            assertThat(response.getStudyMembers().get(1).getMemberId()).isEqualTo(2L);
            assertThat(response.getStudyMembers().get(1).getImageUrl()).isEqualTo("imageUrl2");
            assertThat(response.getStudyMembers().get(1).getName()).isEqualTo("회원2");
            assertThat(response.getStudyMembers().get(1).getRole()).isEqualTo(StudyMemberRole.STUDY_MEMBER);
            assertThat(response.getStudyMembers().get(1).getBadgeLevel()).isEqualTo(BadgeLevel.NONE);


        }

    }

}