package com.tenten.studybadge.participation.controller;

import com.tenten.studybadge.participation.service.StudyChannelParticipationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Study Channel Participation API", description = "스터디 채널 참가 신청에 대한 신청, 취소, 승인, 거절할 수 있는 API")
public class StudyChannelParticipationController {

    private final StudyChannelParticipationService studyChannelParticipationService;

    @PostMapping("/api/study-channels/{studyChannelId}/participation")
    @Operation(summary = "스터디 채널 참가 신청", description = "특정 스터디 채널에 참가 신청을 하는 기능", security = @SecurityRequirement(name = "bearerToken"))
    @Parameter(name = "studyChannelId", description = "참가 신청을 할 스터디 채널 ID", required = true)
    public ResponseEntity<Void> applyParticipation(@PathVariable("studyChannelId") Long studyChannelId) {
        Long memberId = 1L;
        studyChannelParticipationService.apply(studyChannelId, memberId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/participation/{participationId}")
    @Operation(summary = "참가 신청 취소", description = "참가 신청을 취소하는 기능", security = @SecurityRequirement(name = "BearerToken"))
    @Parameter(name = "participationId", description = "참가 신청 ID", required = true)
    public ResponseEntity<Void> cancelParticipation(@PathVariable("participationId") Long participationId) {
        Long memberId = 1L;
        studyChannelParticipationService.cancel(participationId, memberId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/study-channels/{studyChannelId}/participation/{participationId}/approve")
    @Operation(summary = "참가 신청 승인", description = "참가 신청을 승인하는 기능", security = @SecurityRequirement(name = "BearerToken"))
    @Parameter(name = "studyChannelId", description = "스터디 채널 ID", required = true)
    @Parameter(name = "participationId", description = "참가 신청 ID", required = true)
    public ResponseEntity<Void> approveParticipation(
            @PathVariable("studyChannelId") Long studyChannelId,
            @PathVariable("participationId") Long participationId) {
        Long memberId = 2L;
        studyChannelParticipationService.approve(studyChannelId, participationId, memberId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/study-channels/{studyChannelId}/participation/{participationId}/reject")
    @Operation(summary = "참가 신청 거절", description = "참가 신청을 거절하는 기능", security = @SecurityRequirement(name = "BearerToken"))
    @Parameter(name = "studyChannelId", description = "스터디 채널 ID", required = true)
    @Parameter(name = "participationId", description = "참가 신청 ID", required = true)
    public ResponseEntity<Void> rejectParticipation(
            @PathVariable("studyChannelId") Long studyChannelId,
            @PathVariable("participationId") Long participationId) {
        Long memberId = 2L;
        studyChannelParticipationService.reject(studyChannelId, participationId, memberId);
        return ResponseEntity.ok().build();
    }

}
