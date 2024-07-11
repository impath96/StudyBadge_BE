package com.tenten.studybadge.schedule.controller;

import com.tenten.studybadge.schedule.dto.ScheduleCreateRequest;
import com.tenten.studybadge.schedule.dto.ScheduleDeleteRequest;
import com.tenten.studybadge.schedule.dto.ScheduleEditRequest;
import com.tenten.studybadge.schedule.dto.ScheduleResponse;
import com.tenten.studybadge.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Schedule API", description = "특정 study channel의 일정을 등록, 조회, 수정, 삭제할 수 있는 API")
public class ScheduleController {
    private final ScheduleService scheduleService;

    @PostMapping("/study-channels/{studyChannelId}/schedules")
    @Operation(summary = "일정 저장", description = "특정 스터디 채널의 일정을 저장하는 api" ,security = @SecurityRequirement(name = "bearerToken"))
    @Parameter(name = "studyChannelId", description = "일정을 만드는 study channel의 id 값", required = true)
    @Parameter(name = "ScheduleRequest", description = "일정 등록 request, type의 값이 single, repeat에 따라 단일 일정 / 반복 일정 등록으로 나뉜다.", required = true )
    public ResponseEntity<Void> postSchedule(
        @PathVariable Long studyChannelId,
        @Valid @RequestBody ScheduleCreateRequest scheduleCreateRequest)  {
        scheduleService.postSchedule(scheduleCreateRequest, studyChannelId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/study-channels/{studyChannelId}/schedules")
    @Operation(summary = "스터디 채널에 존재하는 일정 전체 조회", description = "특정 스터디 채널에 존재하는 일정 전체 조회 api" ,security = @SecurityRequirement(name = "bearerToken"))
    @Parameter(name = "studyChannelId", description = "일정을 만드는 study channel의 id 값", required = true)
    public ResponseEntity<List<ScheduleResponse>> getSchedules(@PathVariable Long studyChannelId) {
        return ResponseEntity.ok(scheduleService.getSchedulesInStudyChannel(studyChannelId));
    }

    @GetMapping("/study-channels/{studyChannelId}/schedules/date")
    @Operation(summary = "스터디 채널에 존재하는 일정 year, month 기준 전체 조회", description = "특정 스터디 채널에 존재하는 일정들을 year과 month 기준으로 전체 조회 api" ,security = @SecurityRequirement(name = "bearerToken"))
    @Parameter(name = "studyChannelId", description = "일정을 만드는 study channel의 id 값", required = true)
    @Parameter(name = "year", description = "일정의 year 값", required = true)
    @Parameter(name = "month", description = "일정의 month 값", required = true)
    public ResponseEntity<List<ScheduleResponse>> getSchedulesWithFormula(
        @PathVariable Long studyChannelId,
        @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(scheduleService.getSchedulesInStudyChannelForYearAndMonth( studyChannelId, year, month));
    }

    @PutMapping("/study-channels/{studyChannelId}/schedules")
    @Operation(summary = "단일 일정 -> any 일정 | 반복 일정 -> 반복 일정으로 수정", description = "특정 스터디 채널의 일정을 수정할 때 [단일 -> any | 반복 -> 반복]일정으로 수정할 경우 수정 api" ,security = @SecurityRequirement(name = "bearerToken"))
    @Parameter(name = "studyChannelId", description = "일정이 존재하는 study channel의 id 값", required = true)
    @Parameter(name = "ScheduleEditRequest", description = "일정 수정 request, type의 값이 single, repeat에 따라 단일 일정 / 반복 일정 등록으로 나뉜다.", required = true )
    public ResponseEntity<Void> putSchedule(
        @PathVariable Long studyChannelId,
        @Valid @RequestBody ScheduleEditRequest scheduleEditRequest)  {
        scheduleService.putSchedule(studyChannelId, scheduleEditRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/study-channels/{studyChannelId}/schedules/isAfterEvent")
    @Operation(summary = "반복 일정 -> 단일 일정으로 수정", description = "특정 스터디 채널의 일정을 수정할 때 반복 일정에서 단일 일정으로 수정할 경우 수정하는 api" ,security = @SecurityRequirement(name = "bearerToken"))
    @Parameter(name = "studyChannelId", description = "일정이 존재하는 study channel의 id 값", required = true)
    @Parameter(name = "ScheduleEditRequest", description = "일정 등록 request, type의 값이 single, repeat에 따라 단일 일정 / 반복 일정 등록으로 나뉜다.", required = true )
    public ResponseEntity<Void> putRepeatScheduleWithAfterEventSame(
        @PathVariable Long studyChannelId,
        @RequestParam("Same") Boolean isAfterEventSame,
        @Valid @RequestBody ScheduleEditRequest scheduleEditRequest)  {
        scheduleService.putRepeatScheduleWithAfterEventSame(studyChannelId, isAfterEventSame, scheduleEditRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/study-channels/{studyChannelId}/schedules")
    @Operation(summary = "단일 일정 삭제", description = "단일 일정 삭제 api" ,security = @SecurityRequirement(name = "bearerToken"))
    @Parameter(name = "studyChannelId", description = "일정이 존재하는 study channel의 id 값", required = true)
    @Parameter(name = "ScheduleDeleteRequest", description = "일정 삭제 request. 단일/반복 일정에 따라 api경로 자체를 변경했기 때문에 type필드는 없다", required = true )
    public ResponseEntity<Void> deleteSingleSchedule(
        @PathVariable Long studyChannelId,
        @Valid @RequestBody ScheduleDeleteRequest scheduleDeleteRequest) {
        scheduleService.deleteSingleSchedule(studyChannelId, scheduleDeleteRequest);
        return ResponseEntity.ok().build();
    }
}
