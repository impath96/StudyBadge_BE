package com.tenten.studybadge.study.channel.controller;

import com.tenten.studybadge.study.channel.dto.StudyChannelCreateRequest;
import com.tenten.studybadge.study.channel.service.StudyChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class StudyChannelController {

    private final StudyChannelService studyChannelService;

    @PostMapping("/api/study-channels")
    public ResponseEntity<Void> createStudyChannel(@RequestBody @Valid StudyChannelCreateRequest request) {
        // TODO 추후 로그인 기능 완료되면 파라미터로 memberId 를 받아오는 것으로 변경해야 함.
        Long memberId = 1L;
        Long studyChannelId = studyChannelService.create(request, memberId);
        return ResponseEntity
                .created(URI.create("/api/study-channels/" + studyChannelId))
                .build();
    }

}
