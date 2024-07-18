package com.tenten.studybadge.study.member.controller;

import com.tenten.studybadge.common.security.CustomUserDetails;
import com.tenten.studybadge.study.member.dto.AssignRoleRequest;
import com.tenten.studybadge.study.member.dto.StudyMembersResponse;
import com.tenten.studybadge.study.member.service.StudyMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/api/study-channels/{studyChannelId}/members/assign-role")
    @Operation(summary = "서브 리더 역할 부여", description = "스터디 멤버에게 서브 리더 역할을 부여해주는 API")
    @Parameter(name = "studyChannelId", description = "스터디 채널 ID", required = true)
    @Parameter(name = "assignRoleRequest", description = "서브 리더 권한을 부여받을 스터디 멤버 정보", required = true)
    public ResponseEntity<Void> assignSubLeaderRole(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long studyChannelId,
            @Valid @RequestBody AssignRoleRequest assignRoleRequest) {
        studyMemberService.assignStudyLeaderRole(studyChannelId, principal.getId(), assignRoleRequest.getStudyMemberId());
        return ResponseEntity.ok().build();
    }

}
