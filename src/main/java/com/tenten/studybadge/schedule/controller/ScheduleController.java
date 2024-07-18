package com.tenten.studybadge.schedule.controller;

import com.tenten.studybadge.common.security.CustomUserDetails;
import com.tenten.studybadge.schedule.domain.Schedule;
import com.tenten.studybadge.schedule.domain.entity.RepeatSchedule;
import com.tenten.studybadge.schedule.domain.entity.SingleSchedule;
import com.tenten.studybadge.schedule.dto.RepeatScheduleCreateRequest;
import com.tenten.studybadge.schedule.dto.ScheduleDeleteRequest;
import com.tenten.studybadge.schedule.dto.ScheduleEditRequest;
import com.tenten.studybadge.schedule.dto.ScheduleResponse;
import com.tenten.studybadge.schedule.dto.SingleScheduleCreateRequest;
import com.tenten.studybadge.schedule.dto.SingleScheduleEditRequest;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping("/study-channels/{studyChannelId}/single-schedules")
    @Operation(summary = "단일 일정 저장", description = "특정 스터디 채널의 단일 일정을 저장하는 api" ,security = @SecurityRequirement(name = "bearerToken"))
    @Parameter(name = "studyChannelId", description = "일정을 만드는 study channel의 id 값", required = true)
    @Parameter(name = "type")
    @Parameter(name = "ScheduleRequest", description = "단일 일정 등록 request", required = true )
    public ResponseEntity<Void> postSingleSchedule(
        @PathVariable Long studyChannelId,
        @Valid @RequestBody SingleScheduleCreateRequest singleScheduleCreateRequest)  {
        scheduleService.postSingleSchedule(singleScheduleCreateRequest, studyChannelId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/study-channels/{studyChannelId}/repeat-schedules")
    @Operation(summary = "반복 일정 저장", description = "특정 스터디 채널의 반복 일정을 저장하는 api" ,security = @SecurityRequirement(name = "bearerToken"))
    @Parameter(name = "studyChannelId", description = "일정을 만드는 study channel의 id 값", required = true)
    @Parameter(name = "type")
    @Parameter(name = "ScheduleRequest", description = "반복 일정 등록 request", required = true )
    public ResponseEntity<Void> postSingleSchedule(
        @PathVariable Long studyChannelId,
        @Valid @RequestBody RepeatScheduleCreateRequest repeatScheduleCreateRequest)  {
      scheduleService.postRepeatSchedule(repeatScheduleCreateRequest, studyChannelId);
      return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/study-channels/{studyChannelId}/schedules")
    @Operation(summary = "스터디 채널에 존재하는 일정 전체 조회", description = "특정 스터디 채널에 존재하는 일정 전체 조회 api" ,security = @SecurityRequirement(name = "bearerToken"))
    @Parameter(name = "studyChannelId", description = "일정을 만드는 study channel의 id 값", required = true)
    public ResponseEntity<List<ScheduleResponse>> getSchedules(
        @AuthenticationPrincipal CustomUserDetails memberDetails,
        @PathVariable Long studyChannelId) {;
        return ResponseEntity.ok(scheduleService.getSchedulesInStudyChannel(
            memberDetails.getId(), studyChannelId));
    }

    @GetMapping("/study-channels/{studyChannelId}/schedules/date")
    @Operation(summary = "스터디 채널에 존재하는 일정 year, month 기준 전체 조회", description = "특정 스터디 채널에 존재하는 일정들을 year과 month 기준으로 전체 조회 api" ,security = @SecurityRequirement(name = "bearerToken"))
    @Parameter(name = "studyChannelId", description = "일정을 만드는 study channel의 id 값", required = true)
    @Parameter(name = "year", description = "일정의 year 값", required = true)
    @Parameter(name = "month", description = "일정의 month 값", required = true)
    public ResponseEntity<List<ScheduleResponse>> getSchedulesInStudyChannelForYearAndMonth(
        @AuthenticationPrincipal CustomUserDetails memberDetails,
        @PathVariable Long studyChannelId,
        @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(scheduleService.getSchedulesInStudyChannelForYearAndMonth(
            memberDetails.getId(), studyChannelId, year, month));
    }

    @GetMapping("/study-channels/{studyChannelId}/single-schedules/{scheduleId}")
    @Operation(summary = "스터디 채널에 존재하는 단일 일정 자세히 조회", description = "스터디 채널에 존재하는 단일 일정 자세히 조회 api, 일정 등록 알림의 relate URL 관련" ,security = @SecurityRequirement(name = "bearerToken"))
    @Parameter(name = "studyChannelId", description = "일정이 있는 study channel의 id 값", required = true)
    @Parameter(name = "scheduleId", description = "단일 일정 schedule id 값", required = true)
    public ResponseEntity<ScheduleResponse> getSingleScheduleDetail(
        @AuthenticationPrincipal CustomUserDetails memberDetails,
        @PathVariable Long studyChannelId, @PathVariable Long scheduleId) {
        SingleSchedule singleSchedule = scheduleService.getSingleSchedule(memberDetails.getId(),
            studyChannelId, scheduleId);
        return ResponseEntity.ok(singleSchedule.toResponse());
    }

    @GetMapping("/study-channels/{studyChannelId}/repeat-schedules/{scheduleId}")
    @Operation(summary = "스터디 채널에 존재하는 반복 일정 자세히 조회", description = "스터디 채널에 존재하는 반복 일정 자세히 조회 api, 일정 등록 알림의 relate URL 관련" ,security = @SecurityRequirement(name = "bearerToken"))
    @Parameter(name = "studyChannelId", description = "일정이 있는 study channel의 id 값", required = true)
    @Parameter(name = "scheduleId", description = "반복 일정 schedule id 값", required = true)
    public ResponseEntity<ScheduleResponse> getRepeatScheduleDetail(
        @AuthenticationPrincipal CustomUserDetails memberDetails,
        @PathVariable Long studyChannelId, @PathVariable Long scheduleId) {
        RepeatSchedule repeatSchedule = scheduleService.getRepeatSchedule(memberDetails.getId(),
            studyChannelId, scheduleId);
        return ResponseEntity.ok(repeatSchedule.toResponse());
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
    @Parameter(name = "Same", description = "이후 반복 일정도 동일하게 수정할 건지 Boolean 값", required = true)
    @Parameter(name = "ScheduleEditRequest", description = "일정 등록 request, type의 값이 single, repeat에 따라 단일 일정 / 반복 일정 등록으로 나뉜다.", required = true )
    public ResponseEntity<Void> putRepeatScheduleWithAfterEventSame(
        @PathVariable Long studyChannelId,
        @RequestParam("Same") Boolean isAfterEventSame,
        @Valid @RequestBody SingleScheduleEditRequest singleScheduleEditRequest)  {
        scheduleService.putScheduleRepeatToSingle(studyChannelId, isAfterEventSame, singleScheduleEditRequest);
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

    @DeleteMapping("/study-channels/{studyChannelId}/schedules/isAfterEvent")
    @Operation(summary = "반복 일정 삭제", description = "반복 일정 삭제 api" ,security = @SecurityRequirement(name = "bearerToken"))
    @Parameter(name = "studyChannelId", description = "일정이 존재하는 study channel의 id 값", required = true)
    @Parameter(name = "Same", description = "이후 반복 일정도 동일하게 수정할 건지 Boolean 값", required = true)
    @Parameter(name = "ScheduleDeleteRequest", description = "일정 삭제 request. 단일/반복 일정에 따라 api경로 자체를 변경했기 때문에 type필드는 없다", required = true )
    public ResponseEntity<Void> deleteSingleSchedule(
        @PathVariable Long studyChannelId,
        @RequestParam("Same") Boolean isAfterEventSame,
        @Valid @RequestBody ScheduleDeleteRequest scheduleDeleteRequest) {
        scheduleService.deleteRepeatSchedule(studyChannelId, isAfterEventSame, scheduleDeleteRequest);
        return ResponseEntity.ok().build();
    }
}
