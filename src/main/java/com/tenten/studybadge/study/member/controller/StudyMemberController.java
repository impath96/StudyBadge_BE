package com.tenten.studybadge.study.member.controller;

import com.tenten.studybadge.common.security.CustomUserDetails;
import com.tenten.studybadge.study.member.dto.StudyMembersResponse;
import com.tenten.studybadge.study.member.service.StudyMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StudyMemberController {

    private final StudyMemberService studyMemberService;

    @GetMapping("/api/study-channels/{studyChannelId}/members")
    @Operation(summary = "스터디 멤버 리스트 조회", description = "특정 스터디 채널의 스터디 멤버 리스트를 조회하는 API")
    @Parameter(name = "studyChannelId", description = "스터디 채널 ID", required = true)
    public ResponseEntity<StudyMembersResponse> getStudyMembers(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long studyChannelId) {
        return ResponseEntity.ok(studyMemberService.getStudyMembers(studyChannelId, principal.getId()));
    }

}
