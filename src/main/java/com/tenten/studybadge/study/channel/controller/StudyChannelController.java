package com.tenten.studybadge.study.channel.controller;

import com.tenten.studybadge.common.security.CustomUserDetails;
import com.tenten.studybadge.common.utils.PagingUtils;
import com.tenten.studybadge.study.channel.dto.SearchCondition;
import com.tenten.studybadge.study.channel.dto.StudyChannelCreateRequest;
import com.tenten.studybadge.study.channel.dto.StudyChannelListResponse;
import com.tenten.studybadge.study.channel.service.StudyChannelService;
import com.tenten.studybadge.type.study.channel.Category;
import com.tenten.studybadge.type.study.channel.MeetingType;
import com.tenten.studybadge.type.study.channel.RecruitmentStatus;
import com.tenten.studybadge.type.study.channel.SortOrder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    // [ Query ]
    @GetMapping("/study-channels")
    @Operation(summary = "스터디 채널 목록 조회", description = "스터디 채널 목록을 조회하기 위한 API")
    @Parameter(name = "page", description = "조회할 페이지 번호 - 없을 경우 1")
    @Parameter(name = "size", description = "조회할 목록 개수 - 없을 경우 6")
    @Parameter(name = "sort", description = "정렬 방법 - 없을 경우 최신 순, 정렬 기준 : RECENT, POPULAR")
    @Parameter(name = "type", description = "모임 방식 - OFFLINE, ONLINE")
    @Parameter(name = "status", description = "모집 상태 - RECRUITING, RECRUIT_COMPLETED")
    @Parameter(name = "category", description = "카테고리 - IT, LANGUAGE, EMPLOYMENT, SELF_DEVELOPMENT")
    public ResponseEntity<StudyChannelListResponse> getStudyChannels(
        @RequestParam(name = "page", required = false, defaultValue = "1") int page,
        @RequestParam(name = "size", required = false, defaultValue = "6") int size,
        @RequestParam(name = "sort", required = false, defaultValue = "RECENT") SortOrder sortOrder,
        @RequestParam(name = "type", required = false) MeetingType type,
        @RequestParam(name = "status", required = false) RecruitmentStatus status,
        @RequestParam(name = "category", required = false) Category category
    ) {
        return ResponseEntity.ok(studyChannelService.getStudyChannels(PagingUtils.createPageable(page, size, sortOrder), new SearchCondition(type, status, category)));
    }

}
