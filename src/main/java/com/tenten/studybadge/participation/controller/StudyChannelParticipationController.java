package com.tenten.studybadge.participation.controller;

import com.tenten.studybadge.participation.service.StudyChannelParticipationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StudyChannelParticipationController {

    private final StudyChannelParticipationService studyChannelParticipationService;

    @PostMapping("/api/study-channels/{studyChannelId}/participation")
    public ResponseEntity<Void> applyParticipation(@PathVariable("studyChannelId") Long studyChannelId) {
        Long memberId = 1L;
        studyChannelParticipationService.apply(studyChannelId, memberId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/participation/{participationId}")
    public ResponseEntity<Void> cancelParticipation(@PathVariable("participationId") Long participationId) {
        Long memberId = 1L;
        studyChannelParticipationService.cancel(participationId, memberId);
        return ResponseEntity.ok().build();
    }

}
