package com.tenten.studybadge.attendance.controller;

import com.tenten.studybadge.attendance.dto.AttendanceCheckRequest;
import com.tenten.studybadge.attendance.service.AttendanceService;
import com.tenten.studybadge.common.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/api/study-channels/{studyChannelId}/check-attendance")
    public void checkAttendance(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long studyChannelId,
            @Valid @RequestBody AttendanceCheckRequest attendanceCheckRequest) {

        attendanceService.checkAttendance(attendanceCheckRequest, principal.getId(), studyChannelId);

    }
}
