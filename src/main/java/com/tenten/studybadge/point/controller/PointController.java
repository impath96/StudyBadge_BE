package com.tenten.studybadge.point.controller;

import com.tenten.studybadge.common.security.LoginUser;
import com.tenten.studybadge.point.service.PointService;
import com.tenten.studybadge.point.dto.PointResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "PointAPI", description = "PointAPI")
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;

    @Operation(summary = "내 포인트 내역", description = "회원의 포인트 변경 내역을 조회할 수 있는 API", security = @SecurityRequirement(name = "bearerToken"))
    @GetMapping("/my-point")
    public ResponseEntity<List<PointResponse>> getMyPointHistory(@LoginUser Long memberId,
                                                                 @RequestParam(name = "page", defaultValue = "1") int page,
                                                                 @RequestParam(name = "size", defaultValue = "10") int size) {

        List<PointResponse> response = pointService.getMyPointHistory(memberId, page, size);

        return ResponseEntity.ok(response);
    }
}
