package com.tenten.studybadge.study.channel.controller;

import com.tenten.studybadge.common.security.CustomUserDetails;
import com.tenten.studybadge.study.channel.dto.StudyChannelCreateRequest;
import com.tenten.studybadge.study.channel.service.StudyChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Study Channel API", description = "스터디 채널과 관련된 생성, 수정, 삭제 기능을 제공하는 API")
public class StudyChannelController {

    private final StudyChannelService studyChannelService;

    @PostMapping("/study-channels")
    @Operation(summary = "스터디 채널을 생성", description = "스터디 채널을 만들기 위해 사용되는 API", security = @SecurityRequirement(name = "bearerToken"))
    @Parameter(name = "request", description = "스터디 채널을 생성하기 위해 필요한 정보", required = true)
    public ResponseEntity<Void> createStudyChannel(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid StudyChannelCreateRequest request) {
        Long studyChannelId = studyChannelService.create(request, principal.getId());
        return ResponseEntity
                .created(URI.create("/api/study-channels/" + studyChannelId))
                .build();
    }

}
