package com.tenten.studybadge.participation.controller;

import com.tenten.studybadge.common.security.CustomUserDetails;
import com.tenten.studybadge.participation.dto.StudyChannelParticipationStatusResponse;
import com.tenten.studybadge.participation.service.StudyChannelParticipationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Study Channel Participation API", description = "스터디 채널 참가 신청에 대한 신청, 취소, 승인, 거절할 수 있는 API")
public class StudyChannelParticipationController {

    private final StudyChannelParticipationService studyChannelParticipationService;

    @PostMapping("/study-channels/{studyChannelId}/participation")
    @Operation(summary = "스터디 채널 참가 신청", description = "특정 스터디 채널에 참가 신청을 하는 기능", security = @SecurityRequirement(name = "bearerToken"))
    @Parameter(name = "studyChannelId", description = "참가 신청을 할 스터디 채널 ID", required = true)
    public ResponseEntity<Void> applyParticipation(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable("studyChannelId") Long studyChannelId) {
        studyChannelParticipationService.apply(studyChannelId, principal.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/participation/{participationId}")
    @Operation(summary = "참가 신청 취소", description = "참가 신청을 취소하는 기능", security = @SecurityRequirement(name = "BearerToken"))
    @Parameter(name = "participationId", description = "참가 신청 ID", required = true)
    public ResponseEntity<Void> cancelParticipation(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable("participationId") Long participationId) {
        studyChannelParticipationService.cancel(participationId, principal.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/study-channels/{studyChannelId}/participation/{participationId}/approve")
    @Operation(summary = "참가 신청 승인", description = "참가 신청을 승인하는 기능", security = @SecurityRequirement(name = "BearerToken"))
    @Parameter(name = "studyChannelId", description = "스터디 채널 ID", required = true)
    @Parameter(name = "participationId", description = "참가 신청 ID", required = true)
    public ResponseEntity<Void> approveParticipation(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable("studyChannelId") Long studyChannelId,
            @PathVariable("participationId") Long participationId) {
        studyChannelParticipationService.approve(studyChannelId, participationId, principal.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/study-channels/{studyChannelId}/participation/{participationId}/reject")
    @Operation(summary = "참가 신청 거절", description = "참가 신청을 거절하는 기능", security = @SecurityRequirement(name = "BearerToken"))
    @Parameter(name = "studyChannelId", description = "스터디 채널 ID", required = true)
    @Parameter(name = "participationId", description = "참가 신청 ID", required = true)
    public ResponseEntity<Void> rejectParticipation(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable("studyChannelId") Long studyChannelId,
            @PathVariable("participationId") Long participationId) {
        studyChannelParticipationService.reject(studyChannelId, participationId, principal.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/study-channels/{studyChannelId}/participation-status")
    @Operation(summary = "참가 신청 현황 조회", description = "특정 스터디 채널의 모집 상태에 따라 참가 신청 현황을 조회하는 기능", security = @SecurityRequirement(name = "BearerToken"))
    @Parameter(name = "studyChannelId", description = "스터디 채널 ID", required = true)
    public ResponseEntity<StudyChannelParticipationStatusResponse> getStudyChannelParticipationStatus(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long studyChannelId) {
        return ResponseEntity.ok(studyChannelParticipationService.getParticipationStatus(studyChannelId, principal.getId()));
    }
}
