package com.tenten.studybadge.attendance.controller;

import com.tenten.studybadge.attendance.dto.AttendanceCheckRequest;
import com.tenten.studybadge.attendance.service.AttendanceService;
import com.tenten.studybadge.common.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Attendance API", description = "출석과 관련된 기능을 제공하는 API")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/api/study-channels/{studyChannelId}/check-attendance")
    @Operation(summary = "출석 체크 및 갱신", description = "출석 체크와 출석 상태를 변경하는 API", security = @SecurityRequirement(name = "bearerToken"))
    @Parameter(name = "studyChannelId", description = "스터디 채널 ID", required = true)
    @Parameter(name = "attendanceCheckRequest", description = "출석 체크를 위해 필요한 요청 데이터 모음", required = true)
    public ResponseEntity<Void> checkAttendance(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long studyChannelId,
            @Valid @RequestBody AttendanceCheckRequest attendanceCheckRequest) {

        attendanceService.checkAttendance(attendanceCheckRequest, principal.getId(), studyChannelId);
        return ResponseEntity.ok().build();

    }
}
